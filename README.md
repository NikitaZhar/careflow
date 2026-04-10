Commit 10. CleanUp

refactor: simplify MVC controllers and remove duplicated session handling
- Extract helper methods for user retrieval and model population
- Standardize rendering logic across Admin, Client, and Provider controllers
- Preserve existing behavior (all tests passing)
- Improve readability and maintainability following Clean Code and SRP principles

refactor: introduce TimeInterval as a value object for time range logic
- Added validation of time ranges via isValid()
- Implemented intersection logic with edge case handling
- Added contains() and duration() methods
- Centralized time-related logic to eliminate future duplication
- All tests passing

refactor: integrate TimeInterval into schedule entities
- Added toInterval() and hasValidTimeRange() methods
- Centralized time validation logic
- Implemented overlaps() for rules and overrides
- Eliminated duplicated time comparison logic
- All tests passing

refactor: move core business rules into Activity entity
- Added belongsTo(User) and isOwnedBy(Long) methods
- Introduced activate() and deactivate() for status management
- Added isActive() and isInactive() helpers
- Implemented updateDetails(...) to encapsulate field updates
- Improved domain expressiveness and reduced duplication
- All tests passing

refactor: enhance SessionUserValidator with role-specific methods
- Added getValidAdmin(), getValidProvider(), and getValidClient()
- Introduced save() and clear() session management methods
- Preserved existing behavior for backward compatibility
- Improved readability and adherence to Clean Code and SOLID principles
- All tests passing


