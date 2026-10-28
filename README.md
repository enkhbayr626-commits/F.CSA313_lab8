# Лаборатори 8 — Mockito

[![Java CI](https://github.com/enkhbayr626-commits/F.CSA313_lab8/actions/workflows/test.yml/badge.svg?branch=main)](https://github.com/enkhbayr626-commits/F.CSA313_lab8/actions/workflows/test.yml)

С. Энхбаяр / B232270153 / F.CSA313

## Орчин

WSL2, Ubuntu 24.04 дээр ажиллуулав.

`java -version`

```text
openjdk version "21.0.12.1" 2026-08-18
OpenJDK Runtime Environment (build 21.0.12.1+1-1-24.04.4-Ubuntu)
OpenJDK 64-Bit Server VM (build 21.0.12.1+1-1-24.04.4-Ubuntu, mixed mode, sharing)
```

`mvn -version`

```text
Apache Maven 3.8.7
Maven home: /usr/share/maven
Java version: 21.0.12.1, vendor: Ubuntu, runtime: /usr/lib/jvm/java-21-openjdk-amd64
Default locale: en, platform encoding: UTF-8
OS name: "linux", version: "6.6.87.2-microsoft-standard-wsl2", arch: "amd64", family: "unix"
```

## Тест

Төслийн үндсэн хавтсаас WSL дээр ажиллуулах команд:

```bash
mvn test
```

A1–A8-ыг [CourseRegistrationServiceTest.java](src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java) файлд бичсэн.
7 энгийн тест, A8-ийн 3 мөр нийлээд **10 тест** болсон.
Push болон pull request бүрд [GitHub Actions](https://github.com/enkhbayr626-commits/F.CSA313_lab8/actions/workflows/test.yml) дээр `mvn clean test` ажиллана.
Эцсийн [гаралт](results/mvn-test.txt):

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

A8-ийн эхний хоёр мөр оюутны шалгалт дээр буцдаг тул нийтлэг `courseExists` stub ашиглагддаггүй.
Тиймээс зөвхөн тэр stub-д `lenient()` хэрэглэж, бусад stub-ийг strict хэвээр үлдээсэн.

## Мутацийн туршилт

Өөрчлөлт бүрийг тусад нь оруулж, тест ажиллуулсны дараа кодыг сэргээсэн.

| № | Түр хассан код | Унасан тест | Failures / Errors | Гаралт |
|---|---|---|---|---|
| 1 | Идэвхгүй оюутны шалгалт | [A3](src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java#L72), [A8-ийн 2-р мөр](src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java#L138) | 2 / 0 | [Лог 1](results/mvn-test-mutant-1.txt) |
| 2 | `saveRegistration(...)` | [A1](src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java#L50), [A5](src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java#L96) | 2 / 0 | [Лог 2](results/mvn-test-mutant-2.txt) |
| 3 | `courseId == null` | [A6](src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java#L109) | 1 / 0 | [Лог 3](results/mvn-test-mutant-3.txt) |

Гурван мутаци бүгд `BUILD FAILURE` болсон; `UnnecessaryStubbingException` гараагүй.
Эхнийхэд буцах утгын шалгалт, хоёр дахьд `verify`, гурав дахьд `assertThrows` алдааг илрүүлсэн.
A6-д хоёр аргументын `null`-ийг анхнаас нь тус тус шалгасан тул тестийг нэмж засах шаардлага гараагүй.

## Дүгнэлт

Mockito mock-д буцаах утга, шидэх алдааг тохируулж, дуудалтыг нь шалгаж болдог бол fake нь гараар бичсэн хялбаршуулсан хэрэгжүүлэлт байдаг.
Энэ ажилд repository, email илгээгчийг mock болгож, сан болон SMTP сервергүйгээр тестэлсэн.
Харин шалгаж байгаа service болон `Student` зэрэг энгийн өгөгдлийн объектыг mock хийхгүй.
`verify`-г жижиг үйлдэл бүрд хэрэглэвэл логик хэвийн байсан ч кодын дотоод өөрчлөлтөөс тест унах эрсдэлтэй.
Хадгалалтыг хасахад `OK` хэвээр буцсан ч A1, A5 унасан нь зөвхөн буцах утгыг шалгах хангалтгүйг харуулсан.
Мөн `courseId=null` нөхцөлийг тусад нь шалгаснаар гурав дахь мутацийг илрүүлсэн.
Эцэст нь кодыг сэргээж, 10 тест бүгд алдаагүй болсныг дахин шалгасан.
