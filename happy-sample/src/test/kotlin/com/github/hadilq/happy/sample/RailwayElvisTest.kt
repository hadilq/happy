package com.github.hadilq.happy.sample

import com.github.hadilq.happy.annotation.Happy
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeoutException

/**
 * This is a modified version of https://gist.github.com/antonyharfield/1928d02a1163cf115d701deca5b99f63
 */
class RailwayElvisTest {

  @Test
  fun happyPathTest() {
    val result = doWork(input())
    println("result: $result")
    assert(result == Success(Unit))
  }

  // The sample method that needs to compose the processes
  private fun doWork(input: List<String>): Result<Unit> {
    return ((input into
      ::parse elvis
      {
        return Failure("Cannot parse email")
      } into
      ::validate
      ).elvis(
        InvalidEmailAddress = { invalidEmailAddress -> return Failure(invalidEmailAddress.errorMessage) },
        EmptySubject = ::fixEmptySubject,
        EmptyBody = { emptyBody -> return Failure(emptyBody.errorMessage) },
      ) into
      ::send
      ).elvis(
        IOFailure = { ioFailure -> return Failure(ioFailure.error.message ?: "") },
        Timeout = { timeout -> return Failure(timeout.error.message ?: "") },
        UnAuthorized = { unauthorized ->
          (unauthorized.validatedEmail into
            ::authorizeAndSend).elvis { failure ->
            return Failure(failure.errorMessage)
          } into
            { success: Success<Email> ->
              SendSuccess(success.value)
            }
        },
      ) into
      { Success(Unit) }
  }

  private inline infix fun <T, R> T.into(block: (T) -> R): R {
    return block(this)
  }

  data class Email(
    val to: String,
    val subject: String,
    val body: String
  )

  private fun input() = listOf("@sampleTo", "sampleSubject", "sampleBody")

  // Parse the lines of input to an Email object
  private fun parse(inputs: List<String>): Result<Email> =
    if (inputs.size == 3)
      Success(Email(to = inputs[0], subject = inputs[1], body = inputs[2]))
    else
      Failure("Unexpected end of input")

  // Validate Email
  private fun validate(input: Success<Email>): ValidationResult {
    val email = input.value
    return when {
      !email.to.contains("@") -> {
        InvalidEmailAddress("Invalid email address! `To` is ${email.to}")
      }
      email.subject.isBlank() -> {
        EmptySubject(email, "Subject must not be blank")
      }
      email.body.isBlank() -> {
        EmptyBody("Body must not be blank")
      }
      else -> ValidateSuccess(email)
    }
  }

  private fun fixEmptySubject(emptySubject: EmptySubject): ValidateSuccess {
    return ValidateSuccess(emptySubject.email.copy(subject = "NO_SUBJECT"))
  }

  // Send the email (typically this would have an unhappy path too)
  private fun send(input: ValidateSuccess): SendResult {
    val email = input.email
    return try {
      println("Sent to ${email.to}. Whoosh!")
      SendSuccess(email)
    } catch (exception: IOException) {
      IOFailure(exception)
    } catch (exception: TimeoutException) {
      Timeout(exception)
    } catch (exception: UnAuthorizedException) {
      UnAuthorized(input, exception)
    }
  }

  private fun authorizeAndSend(input: ValidateSuccess): Result<Email> {
    val email = input.email
    return try {
      println("Authorize and send again to ${email.to}, WHFFF!")
      Success(email)
    } catch (throwable: Throwable) {
      Failure(throwable.message ?: "")
    }
  }

  sealed class Result<T>

  @Happy
  data class Success<T>(val value: T) : Result<T>()
  data class Failure<T>(val errorMessage: String) : Result<T>()

  sealed class ValidationResult

  @Happy
  data class ValidateSuccess(val email: Email) : ValidationResult()
  data class InvalidEmailAddress(val errorMessage: String) : ValidationResult()
  data class EmptySubject(val email: Email, val errorMessage: String) : ValidationResult()
  data class EmptyBody(val errorMessage: String) : ValidationResult()

  sealed class SendResult

  @Happy
  data class SendSuccess(val email: Email) : SendResult()
  data class IOFailure(val error: IOException) : SendResult()
  data class Timeout(val error: TimeoutException) : SendResult()
  data class UnAuthorized(val validatedEmail: ValidateSuccess, val error: UnAuthorizedException) : SendResult()


  class UnAuthorizedException : Throwable()
}
