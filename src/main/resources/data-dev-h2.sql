-- Seed minimal product for H2 dev profile
INSERT INTO product_catalog (
  product_ean,
  category,
  description,
  currency,
  vendor_endpoint,
  is_posa,
  active,
  min_amount,
  max_amount,
  created_at,
  updated_at
) VALUES (
  'MCF-AV-12M-001',
  'ANTIVIRUS',
  'McAfee Antivirus 12 Months',
  'INR',
  'TEST',
  TRUE,
  TRUE,
  199900,
  199900,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
