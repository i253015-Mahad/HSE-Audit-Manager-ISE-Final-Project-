# ADR-06: Photograph Storage

- **Status:** Accepted

## Context

The HSE Reporting System will support photographs associated with safety reports. The agreed scope is limited to photographs; uploading supporting documents or other file types is not included.

Photograph content should not be stored directly in the relational database. The database still needs to retain the association between a photograph and its report, together with relevant metadata and a reference to the stored file.

## Alternatives Considered

1. **Store file content in the database:** Store photograph binary data directly in SQL database records.
2. **Separate file storage with database references:** Store photograph files outside the SQL database and store their references and relevant metadata in the database.
3. **No attachment support:** Do not support file uploads.

## Decision

Use **separate file storage with database references** for photographs.

- Photograph files will be stored outside the relational database.
- The database will store references to the photographs and relevant metadata.
- Photographs will be associated with their respective safety reports.
- Access to photographs must follow the authorization rules applicable to their associated reports.
- The supported attachment type is photographs only.

The specific file-storage technology or provider is not selected by this ADR.

## Rationale

Separating photograph content from structured business data keeps the relational database focused on application records and avoids unnecessarily increasing its size with binary files. Database references preserve the relationship between photographs and reports.

## Trade-offs / Consequences

- The application must manage both database records and stored files.
- The team must ensure that photograph references remain valid and that file operations are handled safely.
- Photograph access must enforce the same project and role-based authorization boundaries as report access.
- Backups and deployment arrangements must account for both the database and photograph storage.
- Supporting documents and other file types remain outside the agreed scope.
