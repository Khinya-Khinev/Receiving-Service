ALTER TABLE goods_receipts ADD COLUMN receiving_mode VARCHAR(255);
UPDATE goods_receipts SET receiving_mode = 'ASN_MATCHING';
ALTER TABLE goods_receipts ALTER COLUMN receiving_mode SET NOT NULL;
