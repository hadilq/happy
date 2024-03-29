package com.github.hadilq.happy.sample

import com.github.hadilq.happy.annotation.Happy
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeoutException

/**
 * This is a modified version of https://gist.github.com/antonyharfield/1928d02a1163cf115d701deca5b99f63
 */
class RailwayTest {

  @Test
  fun happyPathTest() {
    val result = doWork(input())
    println("result: $result")
    assert(result == Success(Unit))
  }

  // The sample method that needs to compose the processes
  private fun doWork(input: List<String>): Result<Unit> {
    return input into
      ::parse elseIf
      {
        return Failure("Cannot parse email")
      } into
      ::validate elseIf
      {
        InvalidEmailAddress { message -> return Failure(message) }
        EmptySubject(::fixEmptySubject)
        EmptyBody { message -> return Failure(message) }
      } into
      ::send elseIf
      {
        IOFailure { error -> return Failure(error.message ?: "") }
        Timeout { error -> return Failure(error.message ?: "") }
        UnAuthorized { validatedEmail, _ ->
          validatedEmail into
            ::authorizeAndSend elseIf
            {
              return Failure(it)
            } into
            { SendSuccess(validatedEmail.email) }
        }
      } into
      { Success(Unit) }
  }

  inline infix fun <T, R> T.into(block: (T) -> R): R {
    return block(this)
  }

  data class Email(
    val to: String,
    val subject: String,
    val body: String
  )

  fun input() = listOf("@sampleTo", "sampleSubject", "sampleBody")

  // Parse the lines of input to an Email object
  fun parse(inputs: List<String>): Result<Email> =
    if (inputs.size == 3)
      Success(Email(to = inputs[0], subject = inputs[1], body = inputs[2]))
    else
      Failure("Unexpected end of input")

  // Validate Email
  fun validate(input: Success<Email>): ValidationResult {
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

  private fun fixEmptySubject(email: Email, errorMessage: String): ValidateSuccess {
    return ValidateSuccess(email.copy(subject = "NO_SUBJECT"))
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
