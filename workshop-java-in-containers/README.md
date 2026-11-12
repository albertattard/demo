# Java in Containers Deep-Dive

This repository contains examples used in the *Java in Containers Deep-Dive*,
which is part of the JVM and Application Performance workshop.

---

## Disclaimer

The examples provided in this repository are for **educational purposes only**
and are intended to be used **exclusively within this workshop**. While every
effort has been made to ensure accuracy and reliability, these examples are
provided **“as is”**, without any warranties, express or implied.

By using these examples, you acknowledge that you do so **at your own risk**.
The authors and contributors shall **not be held liable** for any direct,
indirect, incidental, or consequential damages resulting from the use, misuse,
or inability to use these examples.

It is the responsibility of the user to **review, test, and validate** any code
before applying it in a production or commercial environment.

### Performance Disclaimer

The performance values shown in this workshop are for reference only and should
not be treated as absolute. Results can vary depending on hardware, system
configuration, runtime environment, and other factors. Readers are encouraged to
conduct their own tests under controlled conditions to obtain accurate
measurements relevant to their specific use case.

---

## References

- [Inside.java: Java into Containers, A Match Made in Heaven?](https://inside.java/2022/04/06/java-in-containers/)
- [Spring Boot: Container Images](https://docs.spring.io/spring-boot/docs/current/reference/html/container-images.html)
- [Spring Boot: Packaging Executable Archives](https://docs.spring.io/spring-boot/maven-plugin/packaging.html)

---

## Prerequisites

The examples included in this workshop require the following.

- [Java 25](https://www.oracle.com/java/technologies/downloads/#java25)
- [Docker](https://www.docker.com/) runtime, such as
  [Podman](https://podman.io/) or [colima](https://github.com/abiosoft/colima)
- [Dive](https://github.com/wagoodman/dive), or other similar tool, to analyse
  the container image.

---


## Examples

Please refer to the [examples `README.md` file](./examples/README.md) for a
complete list of examples.

Example module directories use the form
`<technique-or-concern>-<framework-or-variant>`, such as
`leyden-aot-cache-spring-boot`. This keeps related techniques grouped together
when the same topic is shown with different frameworks.

---

## Feedback and Support

Should you require any assistance in using this workshop or have any feedback,
please reach out to Albert Attard (`albert.attard@oracle.com`).
