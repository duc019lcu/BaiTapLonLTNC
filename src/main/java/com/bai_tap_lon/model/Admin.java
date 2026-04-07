package com.auction.onlineauctionsystem.model;
public class Admin extends User {
    private int accessLevel; 
    public Admin(long id, String username, String password, String email, int accessLevel) {
        super(id, username, password, email);
        this.accessLevel = accessLevel;
    }
    public int getAccessLevel() { return accessLevel; }
    @Override
    public String getRoleName() { return "Admin"; }
}
