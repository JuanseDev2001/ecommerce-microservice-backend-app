output "droplet_id" {
  description = "ID of the Jenkins droplet"
  value       = digitalocean_droplet.jenkins.id
}

output "droplet_ipv4" {
  description = "Public IPv4 address of the Jenkins droplet"
  value       = digitalocean_droplet.jenkins.ipv4_address
}

output "jenkins_url" {
  description = "URL to access Jenkins"
  value       = "http://${digitalocean_droplet.jenkins.ipv4_address}:8080"
}

output "ssh_connection_string" {
  description = "SSH connection string"
  value       = "ssh root@${digitalocean_droplet.jenkins.ipv4_address}"
}
