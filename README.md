![Health Check](https://github.com/hadilq/happy/workflows/Health%20Check/badge.svg?branch=main)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.hadilq/happy/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.hadilq/happy)

# Happy

This library provides an annotation to auto-generate a Kotlin DSL for `sealed` classes to make
working with them more concise.
The standard way to handle different cases of a `sealed` class in Kotlin is using `when` statement/expression.
However, there looks like a more kotliny way to do that, where this annotation processor tries to address.

It named **Happy** to refer to the notion that each `sealed` class as a result have one and only one **happy** path.
Also, you may know Kotlin is `fun`, why not make it `Happy` too?


## How Concise

You may know Kotlin null-safety is what developers love to work with. Did you ask yourself why?
Let's have a closer look. In Kotlin, we have safe call operator, `?.`, and elvis operator, `?:`,
where make working with nulls so easier. The general idea is to implement the same type as
`java.util.Optional` but with shorter names. By the way, it's not the end of story!
Let's look how we use them.
```kotlin
val l = b?.length ?: -1
```
Here the `b?.length` is a process that have two states, either `b` is available and have a `length`
or the result is not available, where it returns `null` and we replace it with `-1`.
Two states? Yes! As mentioned, the null-safety is like `Optional` where has two states.
Basically, it's a sum type.
Sum types or [disjoints](https://en.wikipedia.org/wiki/Coproduct) are types that when you want to think
of them as a result, you use "or" in your sentence, for instance, `b?.length` returns a `value`, as the happy path, **or** `null`.
Generally in Kotlin we use `sealed` classes as sum-types. So the magic of Kotlin in null-safety happens
where we deal with a happy path differently from failed ones. This is the moment of AHA!
Then why `when` in Kotlin doesn't respect to happy paths!
I don't know, but this library wants to fill this gap by introducing this happy DSL to fill the gap.

## Usage
The usage is similar to [Elvis operator](https://kotlinlang.org/docs/reference/null-safety.html#elvis-operator)
especially for two cases `sealed` classes.

### Two Case
For instance, if you have a `sealed` class like
```kotlin
sealed class A {
  @Happy
  object HappyA : A()
  object FailedA : A()
}
```
where it's clear that `HappyA` is the **happy** path if a process uses `A` as the result and `FailedA` is the
failure of the process. Notice we tagged the happy path with `@Happy` annotation.
Let the `doWork` has a result of `A` then the happy DSL would looks like
```kotlin
fun doWork(): A = ....

fun doJob(): B {
  val result: HappyA = doWork() elseIf {
    // Handle the failure.
  }
    ...
}
```
To handle the failure you have three approaches:
 - You can have another method to fix the failure and replace the `FailedA` with `HappyA`,
   for instance bring back the default, or in case of `UnAuthorized`
   exception can request to authorize. It would be like
```kotlin
fun doJob(): B {
  val result: HappyA = doWork() elseIf ::handleFailure
    ...
}
```
- You can break the process and return the failure of `B` type.
```kotlin
fun doJob(): B {
  val result: HappyA = doWork() elseIf {
    return B.failure()
  }
    ...
}
```
- Of course, you can break it with throwing an exception, which is a dirty approach IMHO.
I just mentioned it to have a complete view.
```kotlin
fun doJob(): B {
  val result: HappyA = doWork() elseIf {
    throw ...
  }
    ...
}
```

### Cases' Properties
What will happen if `FailedA` has properties? Not so much difference, but the
lambda function will pass the properties. For instance, assume the following.
```kotlin
sealed class A {
  @Happy
  object HappyA : A()
  class FailedA(val why: Int) : A()
}
```
so the lambda function will be like
```kotlin
fun doJob(): B {
  val result: HappyA = doWork() elseIf { why -> // The property of `FailedA`
    return B.failure(why)
  }
  ...
}
```


### More Than Two Case
For instance, if you have a `sealed` class like
```kotlin
sealed class A {
  @Happy
  class HappyA : A()
  class OptionOne : A()
  class OptionTwo(val why: Int) : A()
}
```
where it's clear that `HappyA` is the `Happy` path if a process uses `A` as the result.
`OptionOne` and `OptionTwo` are the failures.
So the usage will be like
```kotlin
fun doJob(): B {
  val result: HappyA = doWork() elseIf {
      OptionOne(::handleOptionOne)
      OptionTwo { why -> // The property of `OptionTwo`
        return B.failure(why)
      }
  }
  ...
}
```
We assumed that `handleOptionOne` will be able to fix the `OptionOne` failure, but
`OptionTwo` is not fixable, so we returned the failure of `doJob` method.

### Nested Cases
For instance, if you have a sealed class like
```kotlin
sealed class A {
   @Happy
   class HappyA : A()
   abstract class SituationOne : A() {
      class OptionOne : SituationOne()
      class OptionTwo(val why: Int) : SituationOne()
   }

   abstract class SituationTwo : A() {
      class OptionThree(val where: Int) : SituationTwo()
      class OptionFour(val how: Int) : SituationTwo()
   }
}
```
the happy DSL will be like
```kotlin
fun doJob(): B {
   val result: HappyA = doWork() elseIf {
      SituationOneOptionOne(::handleOptionOne)
      SituationOneOptionTwo { why -> // The property of `OptionTwo`
         return B.failure(why)
      }
      SituationTwoOptionThree(::handleOptionThree)
      SituationTwoOptionFour(::handleOptionFour)
   }
   ...
}
```
Did you notice the naming? That's the difference.

## Download

Download via gradle
```groovy
implementation "com.github.hadilq:happy-annotation:$libVersion"
kapt "com.github.hadilq:happy-processor:$libVersion"
```

where the `libVersion` can be found above in `Maven Central` badge.

Snapshots of the development version are available in [Sonatype's snapshots repository](https://oss.sonatype.org/content/repositories/snapshots).

## Contribution

Just create your branch from the main branch, change it, write additional tests, satisfy all tests,
create your pull request, thank you, you're awesome.
