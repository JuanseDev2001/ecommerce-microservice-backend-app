package com.selimhorri.app.helper;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;

public interface UserMappingHelper {
	
	public static UserDto map(final User user) {
		Credential credential = user.getCredential();
		   CredentialDto credentialDto = null;
		   if (credential != null) {
			   credentialDto = CredentialDto.builder()
				   .credentialId(credential.getCredentialId())
				   .username(credential.getUsername())
				   .password(credential.getPassword())
				   .roleBasedAuthority(credential.getRoleBasedAuthority())
				   .isEnabled(credential.getIsEnabled())
				   .isAccountNonExpired(credential.getIsAccountNonExpired())
				   .isAccountNonLocked(credential.getIsAccountNonLocked())
				   .isCredentialsNonExpired(credential.getIsCredentialsNonExpired())
				   .build();
		   }
	       return UserDto.builder()
		   .userId(user.getUserId())
		   .firstName(user.getFirstName())
		   .lastName(user.getLastName())
		   .imageUrl(user.getImageUrl())
		   .email(user.getEmail())
		   .phone(user.getPhone())
		   .credentialDto(credentialDto)
		   .build();
	}
	
	public static User map(final UserDto userDto) {
		User.UserBuilder userBuilder = User.builder()
			.userId(userDto.getUserId())
			.firstName(userDto.getFirstName())
			.lastName(userDto.getLastName())
			.imageUrl(userDto.getImageUrl())
			.email(userDto.getEmail())
			.phone(userDto.getPhone());

		if (userDto.getCredentialDto() != null) {
		    Credential.CredentialBuilder credentialBuilder = Credential.builder()
			    .credentialId(userDto.getCredentialDto().getCredentialId())
			    .username(userDto.getCredentialDto().getUsername())
			    .password(userDto.getCredentialDto().getPassword())
			    .roleBasedAuthority(userDto.getCredentialDto().getRoleBasedAuthority())
			    .isEnabled(userDto.getCredentialDto().getIsEnabled())
			    .isAccountNonExpired(userDto.getCredentialDto().getIsAccountNonExpired())
			    .isAccountNonLocked(userDto.getCredentialDto().getIsAccountNonLocked())
			    .isCredentialsNonExpired(userDto.getCredentialDto().getIsCredentialsNonExpired());

		    Credential credential = credentialBuilder.build();
		    User user = userBuilder.build();
		    credential.setUser(user);
		    user.setCredential(credential);
		    return user;
		} else {
		    return userBuilder.build();
		}
	}
	
	
	
}






