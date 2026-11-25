package com.harmoni.pos.mcp.domain;

public final class CustomerCommands {

    private CustomerCommands() {
    }

    public record CreateCustomerCommand(Integer brandId, String name, String phone, String email) {
    }

    public record UpdateCustomerCommand(Integer brandId, String name, String phone, String email) {
    }
}
