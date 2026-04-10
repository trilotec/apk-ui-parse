# Security Policy

## Reporting a Vulnerability

Do not open a public GitHub issue for security-sensitive problems.

Report privately to the maintainers with:

- affected module
- affected version
- reproduction steps
- impact summary

## Sensitive Data

This project may process accessibility data from running apps. Treat exported JSON
as potentially sensitive. Do not share private captures publicly unless all
sensitive content has been removed.

## Current Security Posture

- Password text masking is enabled by default in dump options
- No automatic upload behavior is included in the library
- Consumers remain responsible for user consent, local storage policy, and data handling
