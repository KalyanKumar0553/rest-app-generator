UPDATE project_tab_definition
SET display_order = CASE
    WHEN tab_key = 'explore' THEN display_order - 10
    WHEN tab_key = 'collaborate' THEN display_order + 10
    ELSE display_order
END,
updated_at = now()
WHERE tab_key IN ('explore', 'collaborate');
