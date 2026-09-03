package com.revethq.documents.service

import com.revethq.iam.permission.domain.Policy
import com.revethq.iam.permission.evaluation.AuthorizationRequest
import com.revethq.iam.permission.evaluation.PolicyCollector
import com.revethq.iam.permission.evaluation.PolicyEvaluator
import com.revethq.iam.permission.web.filter.AuthorizationContext
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject

/**
 * Request-scoped service that filters list results by the authenticated user's policies.
 * Policies are collected once per request and cached for the request duration.
 */
@RequestScoped
class PermissionFilterService
    @Inject
    constructor(
        private val policyEvaluator: PolicyEvaluator,
        private val policyCollector: PolicyCollector,
        private val authorizationContext: AuthorizationContext,
    ) {
        private var cachedPolicies: List<Policy>? = null

        private fun getPolicies(): List<Policy> {
            cachedPolicies?.let { return it }
            val principalUrn = authorizationContext.principalUrn ?: return emptyList()
            val policies = policyCollector.collectPolicies(principalUrn)
            cachedPolicies = policies
            return policies
        }

        /**
         * Filters a list of items by checking if the user has the given action on each item's resource URN.
         *
         * @param items the items to filter
         * @param action the permission action to check (e.g., "documents:GetDocument")
         * @param urnBuilder builds a resource URN for each item
         * @return only items the user's policies permit
         */
        fun <T> filter(
            items: List<T>,
            action: String,
            urnBuilder: (T) -> String,
        ): List<T> {
            val principalUrn = authorizationContext.principalUrn ?: return emptyList()
            val policies = getPolicies()
            if (policies.isEmpty()) return emptyList()

            return items.filter { item ->
                val request =
                    AuthorizationRequest(
                        principalUrn = principalUrn,
                        action = action,
                        resourceUrn = urnBuilder(item),
                    )
                policyEvaluator.evaluateWithPolicies(request, policies).isAllowed()
            }
        }
    }
