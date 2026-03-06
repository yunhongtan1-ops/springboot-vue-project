# FAQGURU source note

This project includes a runtime importer script for public interview question prompts.

Source repository:
- https://github.com/FAQGURU/FAQGURU

License:
- MIT License
- https://github.com/FAQGURU/FAQGURU/blob/master/LICENSE

How this project uses the source:
- The importer downloads markdown files from `topics/en` at runtime.
- It extracts question headings only.
- It does not import the source answers into the local question bank.
- It transforms each heading into a practice prompt for the local application schema.