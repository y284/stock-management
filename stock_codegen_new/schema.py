from models import Table, Column

user = Table(
    name="user",
    remarks="System users with warehouse access",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("username", "VARCHAR(128)", nullable=False, unique=True, is_index=True),
        Column("firstname", "VARCHAR(128)", nullable=False),
        Column("lastname", "VARCHAR(128)", nullable=False),
        Column("rib", "VARCHAR(34)", nullable=True, unique=True),
        Column("email", "VARCHAR(255)", nullable=False, unique=True, is_index=True),
        Column("keycloak_id", "VARCHAR(255)", nullable=False, unique=True),
        Column("warehouse_id", "BIGINT", foreign_key_table="warehouse", foreign_key_column="id", nullable=False, is_index=True),
        Column("is_active", "BOOLEAN", default_value="1"),
    ]
)

warehouse = Table(
    name="warehouse",
    remarks="Warehouse locations belonging to enterprises",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("name", "VARCHAR(255)", nullable=False),
        Column("code", "VARCHAR(32)", nullable=False, unique=True, is_index=True),
        Column("enterprise_id", "BIGINT", foreign_key_table="enterprise", foreign_key_column="id", nullable=False, is_index=True),
    ]
)

enterprise = Table(
    name="enterprise",
    remarks="Business entities that own warehouses",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("name", "VARCHAR(255)", nullable=False, unique=True, is_index=True),
        Column("location", "VARCHAR(255)"),
    ]
)

category = Table(
    name="category",
    remarks="Product categorization with hierarchical support",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("name", "VARCHAR(255)", nullable=False, unique=True, is_index=True),
        Column("parent_id", "BIGINT", foreign_key_table="category", foreign_key_column="id", nullable=True, is_index=True),
    ]
)

product = Table(
    name="product",
    remarks="Product catalog with pricing and categorization",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("description", "VARCHAR(255)", nullable=False, unique=True),
        Column("name", "VARCHAR(64)", nullable=False, is_index=True),
        Column("price", "NUMERIC(12,2)", default_value="0.00"),
        Column("tva", "NUMERIC(5,2)", default_value="0.00"),
        Column("category_id", "BIGINT", foreign_key_table="category", foreign_key_column="id", nullable=True, is_index=True),
        Column("unit_of_measure", "VARCHAR(32)", default_value="'unit'"),
        Column("is_active", "BOOLEAN", default_value="true"),
    ]
)

supplier = Table(
    name="supplier",
    remarks="Product suppliers with banking information",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("fullname", "VARCHAR(255)", nullable=False),
        Column("email", "VARCHAR(255)", unique=True, is_index=True),
        Column("rib", "VARCHAR(34)", nullable=True, unique=True),
        Column("warehouse_id", "BIGINT", foreign_key_table="warehouse", foreign_key_column="id", nullable=False, is_index=True),
        Column("is_active", "BOOLEAN", default_value="true"),
    ]
)

client = Table(
    name="client",
    remarks="Business clients with banking information",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("fullname", "VARCHAR(255)", nullable=False),
        Column("email", "VARCHAR(255)", unique=True, is_index=True),
        Column("rib", "VARCHAR(34)", nullable=True, unique=True),
        Column("warehouse_id", "BIGINT", foreign_key_table="warehouse", foreign_key_column="id", nullable=False, is_index=True),
        Column("is_active", "BOOLEAN", default_value="true"),
    ]
)

purchase_order = Table(
    name="purchase_order",
    remarks="Purchase orders from suppliers",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("order_date", "TIMESTAMPTZ", default_value="NOW()"),
        Column("total_amount", "NUMERIC(12,2)", default_value="0.00"),
        Column("amount_paid", "NUMERIC(12,2)", default_value="0.00"),
        Column("status", "VARCHAR(32)", default_value="'draft'"),
        Column("is_quote", "BOOLEAN", default_value="false"),
        Column("supplier_id", "BIGINT", foreign_key_table="supplier", foreign_key_column="id", nullable=False, is_index=True),
        Column("warehouse_id", "BIGINT", foreign_key_table="warehouse", foreign_key_column="id", nullable=False, is_index=True),
    ]
)

purchase_order_line = Table(
    name="purchase_order_line",
    remarks="Line items in purchase orders",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("purchase_order_id", "BIGINT", foreign_key_table="purchase_order", foreign_key_column="id", nullable=False, is_index=True),
        Column("product_id", "BIGINT", foreign_key_table="product", foreign_key_column="id", nullable=False, is_index=True),
        Column("quantity", "NUMERIC(14,3)", nullable=False, default_value="0"),
        Column("unit_price", "NUMERIC(12,2)", nullable=False, default_value="0.00"),
        Column("discount", "NUMERIC(5,4)", nullable=False, default_value="0.0000"),
    ]
)

sales_order = Table(
    name="sales_order",
    remarks="Sales orders to clients",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("order_date", "TIMESTAMPTZ", default_value="NOW()"),
        Column("total_amount", "NUMERIC(12,2)", default_value="0.00"),
        Column("amount_paid", "NUMERIC(12,2)", default_value="0.00"),
        Column("status", "VARCHAR(32)", default_value="'draft'"),
        Column("is_quote", "BOOLEAN", default_value="false"),
        Column("client_id", "BIGINT", foreign_key_table="client", foreign_key_column="id", nullable=True, is_index=True),
        Column("warehouse_id", "BIGINT", foreign_key_table="warehouse", foreign_key_column="id", nullable=False, is_index=True),
    ]
)

sales_order_line = Table(
    name="sales_order_line",
    remarks="Line items in sales orders",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("sales_order_id", "BIGINT", foreign_key_table="sales_order", foreign_key_column="id", nullable=False, is_index=True),
        Column("product_id", "BIGINT", foreign_key_table="product", foreign_key_column="id", nullable=False, is_index=True),
        Column("quantity", "NUMERIC(14,3)", nullable=False, default_value="0"),
        Column("unit_price", "NUMERIC(12,2)", nullable=False, default_value="0.00"),
        Column("discount", "NUMERIC(5,4)", nullable=False, default_value="0.0000"),
    ]
)

stock_level = Table(
    name="stock_level",
    remarks="Stock quantities by product and warehouse",
    columns=[
        Column("product_id", "BIGINT", primary_key=True, foreign_key_table="product", foreign_key_column="id", nullable=False),
        Column("warehouse_id", "BIGINT", primary_key=True, foreign_key_table="warehouse", foreign_key_column="id", nullable=False),
        Column("current_qty", "NUMERIC(14,3)", default_value="0"),
        Column("reserved_qty", "NUMERIC(14,3)", default_value="0"),
        Column("stock_alert_qty", "NUMERIC(14,3)", default_value="0"),
    ]
)

purchase_invoice = Table(
    name="purchase_invoice",
    remarks="Invoices for supplier purchases",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("purchase_order_id", "BIGINT", foreign_key_table="purchase_order", foreign_key_column="id", nullable=False, is_index=True),
        Column("issue_date", "TIMESTAMPTZ", default_value="NOW()"),
        Column("due_date", "TIMESTAMPTZ"),
        Column("total_amount", "NUMERIC(12,2)", default_value="0.00"),
        Column("paid_amount", "NUMERIC(12,2)", default_value="0.00"),
        Column("status", "VARCHAR(32)", default_value="'pending'"),
    ]
)

sales_invoice = Table(
    name="sales_invoice",
    remarks="Invoices for client sales",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("invoice_number", "BIGINT", nullable=True, is_index=True),
        Column("sales_order_id", "BIGINT", foreign_key_table="sales_order", foreign_key_column="id", nullable=False, is_index=True),
        Column("issue_date", "TIMESTAMPTZ", default_value="NOW()"),
        Column("due_date", "TIMESTAMPTZ"),
        Column("total_amount", "NUMERIC(12,2)", default_value="0.00"),
        Column("paid_amount", "NUMERIC(12,2)", default_value="0.00"),
        Column("status", "VARCHAR(32)", default_value="'pending'"),
    ]
)

payment = Table(
    name="payment",
    remarks="Payments for orders",
    columns=[
        Column("id", "BIGINT", primary_key=True),
        Column("amount", "NUMERIC(12,2)", nullable=False),
        Column("payment_method", "VARCHAR(32)", nullable=False),
        Column("payment_type", "VARCHAR(16)", nullable=False),
        Column("sales_order_id", "BIGINT", foreign_key_table="sales_order", foreign_key_column="id", nullable=True, is_index=True),
    ]
)

tables = [
    user, warehouse, enterprise, category, product, supplier, client,
    purchase_order, purchase_order_line, sales_order, sales_order_line,
    stock_level, purchase_invoice, sales_invoice, payment
]