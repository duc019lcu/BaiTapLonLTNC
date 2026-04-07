package com.auction.onlineauctionsystem.model;
public class Bidder extends User {
    private String shippingAddress; 
    public Bidder(long id, String username, String password, String email, String shippingAddress) {
        super(id, username, password, email);
        this.shippingAddress = shippingAddress;
    }
    public String getShippingAddress() { return shippingAddress; }
    @Override
    public String getRoleName() { return "Bidder"; }
}
