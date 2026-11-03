# Лаборатори №8: Mock объектууд — Mockito-гоор хамаарлыг орлуулж тестлэх

**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)  
**Оюутан:** Б.Мөнгөншагай  
**Оюутны код:** B232270013  
**Үйлдлийн систем:** Arch Linux  
**Node.js:** v26.12.0  
**npm:** 12.2.1  
**Браузер:** Opera 136.0.6008.80  


# Лаборатори №8 — Mock объектууд (Mockito)

**Хичээл:** F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)  
**Оюутан:** Б.Мөнгөншагай  
**Оюутны код:** B232270013  
**Үйлдлийн систем:** Arch Linux  
`java -version` командын гаралт:
```text
openjdk version "17.0.21.0.3" 2026-10-24
OpenJDK Runtime Environment (build 17.0.21.0.3+1)
OpenJDK 64-Bit Server VM (build 17.0.21.0.3+1, mixed mode, sharing)
```
`mvn -version` командын гаралт:
```text
Apache Maven 3.10.0 (c43a36b8d67be7e0805a411bc0898af1a51f5472)
Maven home: /usr/share/java/maven
Java version: 17.0.21.0.3, vendor: Arch Linux, runtime: /usr/lib/jvm/java-17-openjdk
Default locale: en_US, platform encoding: UTF-8, time zone: Asia/Ulaanbaatar
OS name: "linux", version: "6.18.57-2-lts", arch: "amd64", family: "unix"
```

Java 17, Maven, JUnit 5 (Jupiter), Mockito 5.


## Хэсэг A — Mockito-ийн найман тест

Тестүүд `src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java` файлд байрлана.

| № | Нөхцөл ба хүлээгдсэн үр дүн | Ашигласан хэрэгсэл |
|---|---|---|
| A1 | Идэвхтэй оюутан: `OK`; бүртгэл хадгалагдаж, и-мэйл илгээгдэнэ | `when`, `verify`, `eq`, `contains` |
| A2 | Оюутан байхгүй: `ERROR_NO_STUDENT`; хадгалахгүй, и-мэйл илгээхгүй | `never`, `verifyNoInteractions` |
| A3 | Оюутан идэвхгүй: `ERROR_INACTIVE_STUDENT`; хадгалахгүй, и-мэйл илгээхгүй | `verifyNoInteractions`, `never` |
| A4 | Өмнөх холбоо хичээл дутуу: `ERROR_PREREQUISITES`; хадгалахгүй | `when`, `never` |
| A5 | И-мэйл үйлчилгээ exception шидсэн ч бүртгэл хадгалагдаж, `OK` буцна | `doThrow`, `verify` |
| A6 | `studentId` эсвэл `courseId` `null` үед `IllegalArgumentException`; хамаарлуудтай харьцахгүй | `assertThrows`, `verifyNoInteractions` |
| A7 | Илгээсэн и-мэйлийн биед `CS101` хичээлийн код орсон | `ArgumentCaptor` |
| A8 | Байхгүй оюутан, идэвхгүй оюутан, байхгүй хичээлийн алдааны 3 тохиолдлыг шалгана | `@ParameterizedTest`, `@CsvSource`, `lenient` |

A1–A7 нь 7 энгийн тест, A8 нь CSV-ийн 3 мөрөөр ажилладаг тул **нийт 10 тестийн тохиолдол** ажилласан.

`results/mvn-test.txt` файлын сүүлийн дүн:

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**Нотолгоо:** [`results/mvn-test.txt`](results/mvn-test.txt). Эдгээр нь репозиторт хадгалсан бодит Maven гаралтаас авсан тоонууд.

### A8-д `lenient()` ашигласан шалтгаан

A8-ийн `missing` болон `inactive` хувилбаруудад үйлчилгээ `courseExists()`-ийг дуудахгүй тул түүнд бэлдсэн stub ашиглагдахгүй үлдэнэ. Mockito-ийн strict stubbing ийм үед `UnnecessaryStubbingException` үүсгэж болох учир параметржүүлсэн тестийн тэр stub-д `lenient().when(...)` ашигласан.

## 4. Хэсэг B — Мини мутацийн туршилт

Үндсэн кодыг нэг нэгээр нь зориуд өөрчилж, тестүүд алдааг илрүүлж буй эсэхийг шалгасан. Мутаци бүрийн үр дүнг файлд хадгалж, өөрчилсөн эх кодыг буцаан сэргээсэн.

| Мутант | Үндсэн кодод хийсэн түр өөрчлөлт | Унасан тестүүд | Бодит гаралт | Нотолгоо |
|---|---|---|---|---|
| 1 | `if (!student.active())` шалгалтыг comment болгосон | A3 `register_whenStudentInactive_returnsInactiveWithoutEmail`; A8 `register_whenInvalid_returnsExpectedError` (`inactive`) | 2 Failures, 0 Errors; `BUILD FAILURE` | [`mvn-test-mutant-1.txt`](results/mvn-test-mutant-1.txt) |
| 2 | `repository.saveRegistration(studentId, courseId)` дуудлагыг comment болгосон | A1 `register_whenStudentIsActive_returnsOkAndSendsEmail`; A5 `register_whenEmailThrows_returnsOkAndSavesRegistration` | 2 Failures, 0 Errors; `BUILD FAILURE` | [`mvn-test-mutant-2.txt`](results/mvn-test-mutant-2.txt) |
| 3 | Эхний `if` нөхцөлөөс `|| courseId == null` хэсгийг арилгасан | A6 `register_whenEitherInputNull_throwsWithoutInteracting` | 1 Failure, 0 Errors; `BUILD FAILURE` | [`mvn-test-mutant-3.txt`](results/mvn-test-mutant-3.txt) |

### Мутацийн дүгнэлт

Мутант №1-д идэвхгүй оюутны бүртгэлээс татгалзах шалгалт байхгүй болж, A3 болон A8-ийн `inactive` тохиолдол хүлээгдсэн алдааны кодыг аваагүй. Мутант №2-т хадгалах дуудлага байхгүй болсныг A1 болон A5 дахь `verify(repository).saveRegistration(...)` илрүүлсэн. Мутант №3-т `courseId` `null` үед `IllegalArgumentException` үүсээгүй тул A6 тест унасан; A6 нь хоёр оролтын `null` нөхцөлийг анхнаасаа тус тусад нь шалгаж байсан учраас нэмэлт засвар шаардагдаагүй. Иймээс туршсан **3 мутантын 3-ыг** тестүүд илрүүлсэн. Эцэст нь эх кодыг сэргээж, үндсэн 10 тестийг дахин амжилттай ажиллуулсан.

## 5. Ерөнхий дүгнэлт

Энэ лабораторид Mockito ашигласнаар `CourseRegistrationService`-ийн бизнес логикийг өгөгдлийн сан, и-мэйл үйлчилгээ ажиллуулахгүйгээр тестэлсэн. Mock объект нь методын буцаах утга, exception болон хийгдсэн дуудлагыг Mockito-гийн хэрэгслээр тохируулах, шалгах боломжтой. Харин гараар бичсэн fake нь бодит хамаарлын хялбаршуулсан, ажиллах хэрэгжүүлэлт бөгөөд түүнийг тусдаа кодлон арчлах шаардлагатай. `Student` record зэрэг энгийн өгөгдлийн объект болон хамааралгүй цэвэр функцуудыг шаардлагагүйгээр mock хийх нь тестийг төвөгтэй болгодог. Гадаад системтэй харьцдаг `StudentRepository` ба `EmailSender` зэрэг хамаарлыг mock хийх нь нэгжийн тестийн тусгаарлалтыг сайжруулдаг. `verify()`-г хэт олноор хэрэглэвэл тест кодын дотоод дуудлагын дараалал, хэрэгжүүлэлтээс хэт хамаарч, үйлдэл нь зөв хэвээр байхад ч шинэчлэлтийн үед амархан унах эрсдэлтэй. Гурван мутаци тус бүрийг тестүүд барьсан нь одоо шалгаж буй гол нөхцөлүүдийн хувьд тестүүд согог илрүүлэх чадвартайг харуулсан.
Ногоон тестийн гаралт болон гурван мутацийн туршилтын алдааны гаралтууд `results/` хавтаст хадгалагдсан. Эх код `src/main/`, тестийн код `src/test/`, Maven тохиргоо `pom.xml` файлд байна. `target/`, `.idea/`, `.DS_Store` файлуудыг `.gitignore`-оор Git-д оруулахгүй байхаар тохируулсан.
