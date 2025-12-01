#!/bin/bash
# Simplified Jenkins Installation Script for DigitalOcean
set -e

echo "==> Updating system..."
apt-get update
apt-get upgrade -y

echo "==> Installing Java 17..."
apt-get install -y openjdk-17-jdk

echo "==> Installing Jenkins..."
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | apt-key add -
sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
apt-get update
apt-get install -y jenkins

systemctl start jenkins
systemctl enable jenkins

echo "==> Installing Google Cloud SDK..."
echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key --keyring /usr/share/keyrings/cloud.google.gpg add -
apt-get update
apt-get install -y google-cloud-sdk google-cloud-sdk-gke-gcloud-auth-plugin

echo "==> Installing kubectl..."
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

echo "==> Installing Python3, pip and Locust..."
apt-get install -y python3 python3-pip python3-venv
pip3 install --break-system-packages locust

echo "==> Installing Jenkins plugins..."
JENKINS_HOME=/var/lib/jenkins
# Wait for Jenkins to be ready
until curl -s http://localhost:8080 > /dev/null; do
  echo "Waiting for Jenkins to start..."
  sleep 10
done
sleep 30  # Extra time for Jenkins to fully initialize

# Install common plugins (matching your plugins.txt)
jenkins_cli="java -jar /var/cache/jenkins/war/WEB-INF/jenkins-cli.jar -s http://localhost:8080/ -auth admin:$(cat /var/lib/jenkins/secrets/initialAdminPassword)"
PLUGINS="git docker-plugin docker-workflow kubernetes-cli google-cloud-sdk pipeline-stage-view"
for plugin in $PLUGINS; do
  $jenkins_cli install-plugin $plugin || echo "Plugin $plugin already installed or failed"
done
$jenkins_cli restart || systemctl restart jenkins

echo "==> Configuring Jenkins user..."
usermod -aG docker jenkins
mkdir -p /var/lib/jenkins/.kube /var/lib/jenkins/.config/gcloud
chown -R jenkins:jenkins /var/lib/jenkins

# GCP Setup Script
cat > /var/lib/jenkins/setup-gcp.sh << 'EOFGCP'
#!/bin/bash
if [ -f /var/lib/jenkins/gcp-key.json ]; then
  gcloud auth activate-service-account --key-file=/var/lib/jenkins/gcp-key.json
  gcloud config set project ${gcp_project_id}
  gcloud config set compute/region ${gcp_region}
  gcloud auth configure-docker ${gcp_region}-docker.pkg.dev
  echo "✅ GCP configured!"
else
  echo "❌ /var/lib/jenkins/gcp-key.json not found"
fi
EOFGCP

chmod +x /var/lib/jenkins/setup-gcp.sh
chown jenkins:jenkins /var/lib/jenkins/setup-gcp.sh

# Wait for Jenkins
echo "==> Waiting for Jenkins to start..."
until [ -f /var/lib/jenkins/secrets/initialAdminPassword ]; do
  sleep 5
done

# Info file
cat > /root/JENKINS_INFO.txt << EOFINFO
=========================================
🚀 Jenkins Ready!
=========================================

URL: http://\$(curl -s ifconfig.me):8080
User: ${jenkins_admin_user}
Pass: ${jenkins_admin_password}

=========================================
Setup GCP:
=========================================
1. Copy SA key to droplet:
   scp jenkins-key.json root@\$(curl -s ifconfig.me):/var/lib/jenkins/gcp-key.json

2. Run setup:
   sudo -u jenkins /var/lib/jenkins/setup-gcp.sh

3. Test:
   sudo -u jenkins gcloud projects list

=========================================
EOFINFO

cat /root/JENKINS_INFO.txt
echo "==> ✅ Installation complete!"
