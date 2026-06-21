package com.unitynews.server.domain.model

/**
 * Demo scenarios controlled by the backend console.
 *
 * They let reviewers verify reader behavior for success, slow responses,
 * empty responses, server errors, and authorization failures.
 */
enum class ServerScenario {
    Normal,
    Slow,
    Empty,
    ServerError,
    Unauthorized,
}
