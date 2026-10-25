package com.voltledger.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {
    @NotBlank @Pattern(regexp="[a-z][a-z0-9_]{2,29}", message="Use 3-30 lowercase letters, numbers or underscores (start with a letter)")
    private String username="";
    @NotBlank @Size(min=2,max=100,message="Name must contain 2-100 characters")
    private String displayName="";
    @NotBlank @Size(min=12,max=64,message="Password must contain 12-64 characters")
    private String password="";
    @NotBlank
    private String confirmPassword="";
    public String getUsername(){return username;} public void setUsername(String s){username=s;}
    public String getDisplayName(){return displayName;} public void setDisplayName(String s){displayName=s;}
    public String getPassword(){return password;} public void setPassword(String s){password=s;}
    public String getConfirmPassword(){return confirmPassword;} public void setConfirmPassword(String s){confirmPassword=s;}
}
