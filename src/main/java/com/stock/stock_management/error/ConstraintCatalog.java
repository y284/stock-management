package com.stock.stock_management.error;

import java.util.*;

public final class ConstraintCatalog {
    private static final Map<String, ErrorCode> byConstraint = new HashMap<>();

    static {
        byConstraint.put("uk_users_username".toLowerCase(java.util.Locale.ROOT), ErrorCode.USERS_USERNAME_DUPLICATE);
        byConstraint.put("nn_users_username".toLowerCase(java.util.Locale.ROOT), ErrorCode.USERS_USERNAME_REQUIRED);
        byConstraint.put("nn_users_firstname".toLowerCase(java.util.Locale.ROOT), ErrorCode.USERS_FIRSTNAME_REQUIRED);
        byConstraint.put("nn_users_lastname".toLowerCase(java.util.Locale.ROOT), ErrorCode.USERS_LASTNAME_REQUIRED);
        byConstraint.put("uk_users_email".toLowerCase(java.util.Locale.ROOT), ErrorCode.USERS_EMAIL_DUPLICATE);
        byConstraint.put("nn_users_email".toLowerCase(java.util.Locale.ROOT), ErrorCode.USERS_EMAIL_REQUIRED);
        byConstraint.put("uk_users_keycloak_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.USERS_KEYCLOAK_ID_DUPLICATE);
        byConstraint.put("nn_users_keycloak_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.USERS_KEYCLOAK_ID_REQUIRED);
        byConstraint.put("fk_users_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.USERS_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_users_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.USERS_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("nn_warehouse_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.WAREHOUSE_NAME_REQUIRED);
        byConstraint.put("uk_warehouse_code".toLowerCase(java.util.Locale.ROOT), ErrorCode.WAREHOUSE_CODE_DUPLICATE);
        byConstraint.put("nn_warehouse_code".toLowerCase(java.util.Locale.ROOT), ErrorCode.WAREHOUSE_CODE_REQUIRED);
        byConstraint.put("fk_warehouse_enterprise_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.WAREHOUSE_ENTERPRISE_ID_FK_VIOLATION);
        byConstraint.put("nn_warehouse_enterprise_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.WAREHOUSE_ENTERPRISE_ID_REQUIRED);
        byConstraint.put("uk_enterprise_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.ENTERPRISE_NAME_DUPLICATE);
        byConstraint.put("nn_enterprise_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.ENTERPRISE_NAME_REQUIRED);
        byConstraint.put("uk_category_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.CATEGORY_NAME_DUPLICATE);
        byConstraint.put("nn_category_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.CATEGORY_NAME_REQUIRED);
        byConstraint.put("fk_category_parent_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.CATEGORY_PARENT_ID_FK_VIOLATION);
        byConstraint.put("uk_product_sku".toLowerCase(java.util.Locale.ROOT), ErrorCode.PRODUCT_SKU_DUPLICATE);
        byConstraint.put("nn_product_sku".toLowerCase(java.util.Locale.ROOT), ErrorCode.PRODUCT_SKU_REQUIRED);
        byConstraint.put("nn_product_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.PRODUCT_NAME_REQUIRED);
        byConstraint.put("fk_product_category_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PRODUCT_CATEGORY_ID_FK_VIOLATION);
        byConstraint.put("nn_supplier_fullname".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_FULLNAME_REQUIRED);
        byConstraint.put("uk_supplier_email".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_EMAIL_DUPLICATE);
        byConstraint.put("uk_supplier_iban".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_IBAN_DUPLICATE);
        byConstraint.put("nn_supplier_iban".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_IBAN_REQUIRED);
        byConstraint.put("fk_supplier_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_supplier_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("nn_client_fullname".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_FULLNAME_REQUIRED);
        byConstraint.put("uk_client_email".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_EMAIL_DUPLICATE);
        byConstraint.put("uk_client_iban".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_IBAN_DUPLICATE);
        byConstraint.put("nn_client_iban".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_IBAN_REQUIRED);
        byConstraint.put("fk_client_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_client_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("fk_purchase_order_supplier_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDER_SUPPLIER_ID_FK_VIOLATION);
        byConstraint.put("nn_purchase_order_supplier_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDER_SUPPLIER_ID_REQUIRED);
        byConstraint.put("fk_purchase_order_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDER_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_purchase_order_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDER_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("fk_purchase_order_line_purchase_order_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDERLINE_PURCHASE_ORDER_ID_FK_VIOLATION);
        byConstraint.put("nn_purchase_order_line_purchase_order_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDERLINE_PURCHASE_ORDER_ID_REQUIRED);
        byConstraint.put("fk_purchase_order_line_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDERLINE_PRODUCT_ID_FK_VIOLATION);
        byConstraint.put("nn_purchase_order_line_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDERLINE_PRODUCT_ID_REQUIRED);
        byConstraint.put("nn_purchase_order_line_quantity".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDERLINE_QUANTITY_REQUIRED);
        byConstraint.put("nn_purchase_order_line_unit_price".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDERLINE_UNIT_PRICE_REQUIRED);
        byConstraint.put("nn_purchase_order_line_discount".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEORDERLINE_DISCOUNT_REQUIRED);
        byConstraint.put("fk_sales_order_client_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDER_CLIENT_ID_FK_VIOLATION);
        byConstraint.put("nn_sales_order_client_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDER_CLIENT_ID_REQUIRED);
        byConstraint.put("fk_sales_order_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDER_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_sales_order_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDER_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("fk_sales_order_line_sales_order_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDERLINE_SALES_ORDER_ID_FK_VIOLATION);
        byConstraint.put("nn_sales_order_line_sales_order_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDERLINE_SALES_ORDER_ID_REQUIRED);
        byConstraint.put("fk_sales_order_line_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDERLINE_PRODUCT_ID_FK_VIOLATION);
        byConstraint.put("nn_sales_order_line_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDERLINE_PRODUCT_ID_REQUIRED);
        byConstraint.put("nn_sales_order_line_quantity".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDERLINE_QUANTITY_REQUIRED);
        byConstraint.put("nn_sales_order_line_unit_price".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDERLINE_UNIT_PRICE_REQUIRED);
        byConstraint.put("nn_sales_order_line_discount".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESORDERLINE_DISCOUNT_REQUIRED);
        byConstraint.put("fk_stock_level_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.STOCKLEVEL_PRODUCT_ID_FK_VIOLATION);
        byConstraint.put("nn_stock_level_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.STOCKLEVEL_PRODUCT_ID_REQUIRED);
        byConstraint.put("fk_stock_level_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.STOCKLEVEL_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_stock_level_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.STOCKLEVEL_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("fk_purchase_invoice_purchase_order_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEINVOICE_PURCHASE_ORDER_ID_FK_VIOLATION);
        byConstraint.put("nn_purchase_invoice_purchase_order_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PURCHASEINVOICE_PURCHASE_ORDER_ID_REQUIRED);
        byConstraint.put("fk_sales_invoice_sales_order_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESINVOICE_SALES_ORDER_ID_FK_VIOLATION);
        byConstraint.put("nn_sales_invoice_sales_order_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALESINVOICE_SALES_ORDER_ID_REQUIRED);
        byConstraint.put("nn_payment_amount".toLowerCase(java.util.Locale.ROOT), ErrorCode.PAYMENT_AMOUNT_REQUIRED);
        byConstraint.put("nn_payment_payment_method".toLowerCase(java.util.Locale.ROOT), ErrorCode.PAYMENT_PAYMENT_METHOD_REQUIRED);
        byConstraint.put("nn_payment_payment_type".toLowerCase(java.util.Locale.ROOT), ErrorCode.PAYMENT_PAYMENT_TYPE_REQUIRED);
        byConstraint.put("fk_payment_sales_order_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PAYMENT_SALES_ORDER_ID_FK_VIOLATION);
    }

    private ConstraintCatalog() {}

    /** @return Specific ErrorCode for a DB/DDL constraint name (case-insensitive), or empty if unknown. */
    public static Optional<ErrorCode> resolve(String constraintName) {
        if (constraintName == null) return Optional.empty();
        String key = constraintName.toLowerCase(Locale.ROOT);
        ErrorCode code = byConstraint.get(key);
        return Optional.ofNullable(code);
    }
}
