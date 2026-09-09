package com.zioanacleto.feedtracker.features.auth.routes

import com.zioanacleto.feedtracker.common.models.ApiResponse
import com.zioanacleto.feedtracker.config.configureDI
import com.zioanacleto.feedtracker.domain.auth.AuthMethod
import com.zioanacleto.feedtracker.domain.auth.AuthMethodsResponse
import com.zioanacleto.feedtracker.domain.auth.AuthSession
import com.zioanacleto.feedtracker.domain.auth.UserModel
import com.zioanacleto.feedtracker.domain.auth.VerifyEmailCodeResponse
import com.zioanacleto.feedtracker.features.auth.services.AuthService
import com.zioanacleto.feedtracker.features.trackingsessions.services.TrackingSessionService
import com.zioanacleto.feedtracker.installTestConfig
import com.zioanacleto.feedtracker.testModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.koin.dsl.module

class AuthRoutesTest :
    FunSpec({

        val json = Json { ignoreUnknownKeys = true }
        val session = AuthSession(
            user = UserModel(
                id = "user-1",
                email = "mario@example.com",
                authMethods = listOf(AuthMethod.EMAIL),
                firstName = "Mario",
                lastName = "Rossi",
            ),
            accessToken = "access-token",
        )

        test("GET /api/auth/methods returns configured methods") {
            val mockAuth = mockk<AuthService>()
            every { mockAuth.availableAuthMethods() } returns listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE)

            testApplication {
                installTestConfig()
                application {
                    testModule {
                        configureDI(
                            extraModules = listOf(
                                module {
                                    single<AuthService> { mockAuth }
                                    single<TrackingSessionService> { mockk(relaxed = true) }
                                },
                            ),
                        )
                    }
                }

                client.get("/api/auth/methods").apply {
                    status shouldBe HttpStatusCode.OK
                    val response = json.decodeFromString<ApiResponse<AuthMethodsResponse>>(bodyAsText())
                    response.data?.methods shouldBe listOf(AuthMethod.EMAIL, AuthMethod.GOOGLE)
                }
            }
        }

        test("POST /api/auth/email/start returns 202") {
            val mockAuth = mockk<AuthService>()
            coEvery { mockAuth.startEmailRegistration(any()) } returns Unit

            testApplication {
                installTestConfig()
                application {
                    testModule {
                        configureDI(
                            extraModules = listOf(
                                module {
                                    single<AuthService> { mockAuth }
                                    single<TrackingSessionService> { mockk(relaxed = true) }
                                },
                            ),
                        )
                    }
                }

                client.post("/api/auth/email/start") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"email":"mario@example.com"}""")
                }.apply {
                    status shouldBe HttpStatusCode.Accepted
                }
            }
        }

        test("POST /api/auth/email/verify returns a registration token") {
            val mockAuth = mockk<AuthService>()
            coEvery { mockAuth.verifyEmailCode(any()) } returns VerifyEmailCodeResponse("reg-token")

            testApplication {
                installTestConfig()
                application {
                    testModule {
                        configureDI(
                            extraModules = listOf(
                                module {
                                    single<AuthService> { mockAuth }
                                    single<TrackingSessionService> { mockk(relaxed = true) }
                                },
                            ),
                        )
                    }
                }

                client.post("/api/auth/email/verify") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"email":"mario@example.com","code":"123456"}""")
                }.apply {
                    status shouldBe HttpStatusCode.OK
                    val response = json.decodeFromString<ApiResponse<VerifyEmailCodeResponse>>(bodyAsText())
                    response.data?.registrationToken shouldBe "reg-token"
                }
            }
        }

        test("POST /api/auth/google returns a session") {
            val mockAuth = mockk<AuthService>()
            coEvery { mockAuth.loginWithGoogle(any()) } returns session.copy(
                user = session.user.copy(authMethods = listOf(AuthMethod.GOOGLE)),
            )

            testApplication {
                installTestConfig()
                application {
                    testModule {
                        configureDI(
                            extraModules = listOf(
                                module {
                                    single<AuthService> { mockAuth }
                                    single<TrackingSessionService> { mockk(relaxed = true) }
                                },
                            ),
                        )
                    }
                }

                client.post("/api/auth/google") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"idToken":"google-token"}""")
                }.apply {
                    status shouldBe HttpStatusCode.OK
                    val response = json.decodeFromString<ApiResponse<AuthSession>>(bodyAsText())
                    response.data?.user?.authMethods shouldBe listOf(AuthMethod.GOOGLE)
                }
            }
        }

        test("PATCH /api/auth/profile updates the current user") {
            val mockAuth = mockk<AuthService>()
            coEvery { mockAuth.updateProfile("access-token", any()) } returns session.user.copy(
                firstName = "Luigi",
                lastName = "Bianchi",
            )

            testApplication {
                installTestConfig()
                application {
                    testModule {
                        configureDI(
                            extraModules = listOf(
                                module {
                                    single<AuthService> { mockAuth }
                                    single<TrackingSessionService> { mockk(relaxed = true) }
                                },
                            ),
                        )
                    }
                }

                client.patch("/api/auth/profile") {
                    header(HttpHeaders.Authorization, "Bearer access-token")
                    contentType(ContentType.Application.Json)
                    setBody("""{"firstName":"Luigi","lastName":"Bianchi"}""")
                }.apply {
                    status shouldBe HttpStatusCode.OK
                    val response = json.decodeFromString<ApiResponse<UserModel>>(bodyAsText())
                    response.data?.firstName shouldBe "Luigi"
                    response.data?.lastName shouldBe "Bianchi"
                }
            }
        }

        test("POST /api/auth/logout revokes the access token") {
            val mockAuth = mockk<AuthService>()
            coEvery { mockAuth.logout("access-token") } returns Unit

            testApplication {
                installTestConfig()
                application {
                    testModule {
                        configureDI(
                            extraModules = listOf(
                                module {
                                    single<AuthService> { mockAuth }
                                    single<TrackingSessionService> { mockk(relaxed = true) }
                                },
                            ),
                        )
                    }
                }

                client.post("/api/auth/logout") {
                    header(HttpHeaders.Authorization, "Bearer access-token")
                }.apply {
                    status shouldBe HttpStatusCode.OK
                    val response = json.decodeFromString<ApiResponse<Unit>>(bodyAsText())
                    response.status shouldBe "SUCCESS"
                }
            }
        }

        test("POST /api/auth/logout without a token returns 401") {
            val mockAuth = mockk<AuthService>()

            testApplication {
                installTestConfig()
                application {
                    testModule {
                        configureDI(
                            extraModules = listOf(
                                module {
                                    single<AuthService> { mockAuth }
                                    single<TrackingSessionService> { mockk(relaxed = true) }
                                },
                            ),
                        )
                    }
                }

                client.post("/api/auth/logout").apply {
                    status shouldBe HttpStatusCode.Unauthorized
                }
            }
        }
    })
