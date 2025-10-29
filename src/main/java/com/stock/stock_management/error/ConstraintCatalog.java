package com.stock.stock_management.error;

import java.util.*;

public final class ConstraintCatalog {
    private static final Map<String, ErrorCode> byConstraint = new HashMap<>();

    static {
        byConstraint.put("uk_entreprise_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.ENTREPRISE_NAME_DUPLICATE);
        byConstraint.put("nn_entreprise_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.ENTREPRISE_NAME_REQUIRED);
        byConstraint.put("uk_warehouse_code".toLowerCase(java.util.Locale.ROOT), ErrorCode.WAREHOUSE_CODE_DUPLICATE);
        byConstraint.put("nn_warehouse_code".toLowerCase(java.util.Locale.ROOT), ErrorCode.WAREHOUSE_CODE_REQUIRED);
        byConstraint.put("nn_warehouse_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.WAREHOUSE_NAME_REQUIRED);
        byConstraint.put("fk_warehouse_entreprise_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.WAREHOUSE_ENTREPRISE_ID_FK_VIOLATION);
        byConstraint.put("nn_warehouse_entreprise_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.WAREHOUSE_ENTREPRISE_ID_REQUIRED);
        byConstraint.put("uk_user_username".toLowerCase(java.util.Locale.ROOT), ErrorCode.USER_USERNAME_DUPLICATE);
        byConstraint.put("nn_user_username".toLowerCase(java.util.Locale.ROOT), ErrorCode.USER_USERNAME_REQUIRED);
        byConstraint.put("fk_user_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.USER_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("uk_category_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.CATEGORY_NAME_DUPLICATE);
        byConstraint.put("nn_category_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.CATEGORY_NAME_REQUIRED);
        byConstraint.put("fk_category_parent_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.CATEGORY_PARENT_ID_FK_VIOLATION);
        byConstraint.put("uk_product_sku".toLowerCase(java.util.Locale.ROOT), ErrorCode.PRODUCT_SKU_DUPLICATE);
        byConstraint.put("nn_product_sku".toLowerCase(java.util.Locale.ROOT), ErrorCode.PRODUCT_SKU_REQUIRED);
        byConstraint.put("nn_product_name".toLowerCase(java.util.Locale.ROOT), ErrorCode.PRODUCT_NAME_REQUIRED);
        byConstraint.put("fk_product_category_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.PRODUCT_CATEGORY_ID_FK_VIOLATION);
        byConstraint.put("fk_stock_level_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.STOCKLEVEL_PRODUCT_ID_FK_VIOLATION);
        byConstraint.put("nn_stock_level_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.STOCKLEVEL_PRODUCT_ID_REQUIRED);
        byConstraint.put("fk_stock_level_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.STOCKLEVEL_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_stock_level_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.STOCKLEVEL_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("nn_client_fullname".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_FULLNAME_REQUIRED);
        byConstraint.put("uk_client_email".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_EMAIL_DUPLICATE);
        byConstraint.put("uk_client_rib".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_RIB_DUPLICATE);
        byConstraint.put("nn_client_rib".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_RIB_REQUIRED);
        byConstraint.put("fk_client_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_client_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.CLIENT_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("nn_supplier_fullname".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_FULLNAME_REQUIRED);
        byConstraint.put("uk_supplier_email".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_EMAIL_DUPLICATE);
        byConstraint.put("uk_supplier_rib".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_RIB_DUPLICATE);
        byConstraint.put("nn_supplier_rib".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_RIB_REQUIRED);
        byConstraint.put("fk_supplier_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_supplier_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIER_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("nn_sale_commande_quantity".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDE_QUANTITY_REQUIRED);
        byConstraint.put("nn_sale_commande_price".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDE_PRICE_REQUIRED);
        byConstraint.put("fk_sale_commande_client_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDE_CLIENT_ID_FK_VIOLATION);
        byConstraint.put("nn_sale_commande_client_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDE_CLIENT_ID_REQUIRED);
        byConstraint.put("fk_sale_commande_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDE_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_sale_commande_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDE_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("fk_sale_commande_line_sale_commande_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDELINE_SALE_COMMANDE_ID_FK_VIOLATION);
        byConstraint.put("nn_sale_commande_line_sale_commande_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDELINE_SALE_COMMANDE_ID_REQUIRED);
        byConstraint.put("fk_sale_commande_line_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDELINE_PRODUCT_ID_FK_VIOLATION);
        byConstraint.put("nn_sale_commande_line_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDELINE_PRODUCT_ID_REQUIRED);
        byConstraint.put("nn_sale_commande_line_quantity".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDELINE_QUANTITY_REQUIRED);
        byConstraint.put("nn_sale_commande_line_price".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDELINE_PRICE_REQUIRED);
        byConstraint.put("nn_sale_commande_line_discount".toLowerCase(java.util.Locale.ROOT), ErrorCode.SALECOMMANDELINE_DISCOUNT_REQUIRED);
        byConstraint.put("nn_supplier_commande_quantity".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDE_QUANTITY_REQUIRED);
        byConstraint.put("nn_supplier_commande_price".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDE_PRICE_REQUIRED);
        byConstraint.put("fk_supplier_commande_supplier_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDE_SUPPLIER_ID_FK_VIOLATION);
        byConstraint.put("fk_supplier_commande_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDE_WAREHOUSE_ID_FK_VIOLATION);
        byConstraint.put("nn_supplier_commande_warehouse_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDE_WAREHOUSE_ID_REQUIRED);
        byConstraint.put("fk_supplier_commande_line_supplier_commande_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDELINE_SUPPLIER_COMMANDE_ID_FK_VIOLATION);
        byConstraint.put("nn_supplier_commande_line_supplier_commande_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDELINE_SUPPLIER_COMMANDE_ID_REQUIRED);
        byConstraint.put("fk_supplier_commande_line_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDELINE_PRODUCT_ID_FK_VIOLATION);
        byConstraint.put("nn_supplier_commande_line_product_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDELINE_PRODUCT_ID_REQUIRED);
        byConstraint.put("nn_supplier_commande_line_quantity".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDELINE_QUANTITY_REQUIRED);
        byConstraint.put("nn_supplier_commande_line_price".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDELINE_PRICE_REQUIRED);
        byConstraint.put("nn_supplier_commande_line_discount".toLowerCase(java.util.Locale.ROOT), ErrorCode.SUPPLIERCOMMANDELINE_DISCOUNT_REQUIRED);
        byConstraint.put("fk_invoice_client_sale_commande_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.INVOICECLIENT_SALE_COMMANDE_ID_FK_VIOLATION);
        byConstraint.put("nn_invoice_client_sale_commande_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.INVOICECLIENT_SALE_COMMANDE_ID_REQUIRED);
        byConstraint.put("fk_invoice_supplier_supplier_commande_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.INVOICESUPPLIER_SUPPLIER_COMMANDE_ID_FK_VIOLATION);
        byConstraint.put("nn_invoice_supplier_supplier_commande_id".toLowerCase(java.util.Locale.ROOT), ErrorCode.INVOICESUPPLIER_SUPPLIER_COMMANDE_ID_REQUIRED);
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
