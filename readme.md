# PicoPica

**Tiny, dependency-free conditional compilation for any language using `/* ... */` comments**.

PicoPica is a pico-sized **source code preprocessor** that lets you keep multiple versions of your project — like open source vs. premium — **in a single codebase**, inspired by Lukas Eder’s article [How to Support Java 6, 8, 9 in a Single API](https://blog.jooq.org/how-to-support-java-6-8-9-in-a-single-api/).

It works with Java, but also any language that supports `/* ... */` block comments.

* Size: 36 KB
* Modern: Requires Java 17+
* Preserves formatting: Byte-for-byte for non-directive code
* Safe: Handles nested directives, string literals, and comment quirks
* External Dependencies: None

## 💡 Use Cases

PicoPica is particularly useful for:

*   **Maintaining Open Source and Premium Versions**: Easily include or exclude features based on whether a premium key is active.
*   **Cross-Version Compatibility**: Target different language versions or platforms from a single source.
*   **Feature Flagging**: Conditionally enable/disable experimental features.


## 📄 License & Source Code

PicoPica is released under the Apache License, Version 2.0. See the `LICENSE` file for more details.

Source code is available at: https://github.com/ainslec/picopica

## 📊 How It Compares

| Feature / Tool                         | **PicoPica**                                               | preprocess (Node.js)                          | GPP (Generic Preprocessor) | Java Preprocessor (JPP)           | Custom Script (sed/awk/Python) |
| -------------------------------------- | ---------------------------------------------------------- | --------------------------------------------- | -------------------------- | --------------------------------- | ------------------------------ |
| **Language support**                   | Any language with block comments (`/*...*/`, `<!--...-->`) | Many (but must match comment syntax manually) | Any text file              | Java only                         | Any text file                  |
| **Formatting preserved**               | ✅ Yes, byte-for-byte for non-directive code                | ❌ Not guaranteed                              | ❌ No                       | ❌ No                              | ❌ No                           |
| **Inline parameter toggling**          | ✅ Clean, safe for commas and spaces                        | ⚠️ Possible but messy                         | ❌ Not designed for inline  | ❌ Not designed for inline         | ❌ Fragile                      |
| **Nested directives**                  | ✅ Fully supported                                          | ⚠️ Limited                                    | ❌ Difficult                | ❌ No                              | ❌ No                           |
| **Works inside Javadoc/HTML comments** | ✅ Yes                                                      | ⚠️ Possible with custom syntax                | ❌ No                       | ❌ No                              | ❌ Possible but fragile         |
| **License header injection**           | ✅ Built-in                                                 | ❌ No                                          | ❌ No                       | ❌ No                              | ❌ No                           |
| **Dependencies**                       | Java                                                       | Node.js                                       | CLI tool                   | Java                              | Script runtime                 |
| **Binary size**                        | \~32 KB                                                    | \~200 KB+                                     | \~1 MB                     | \~100 KB                          | N/A                            |
| **Setup complexity**                   | Low (drop-in jar)                                          | Medium (Node install)                         | Medium (install binary)    | Medium (Java integration)         | Medium–High (custom code)      |
| **Best for**                           | Multi-version source mgmt with perfect formatting          | Multi-env web projects                        | Heavy macro preprocessing  | Java-only conditional compilation | One-off quick hacks            |

---


| Directive Type                   | Syntax                                                 | When It’s Kept                                                                                         |
| -------------------------------- | ------------------------------------------------------ | ------------------------------------------------------------------------------------------------------ |
| **Include Block**                | `/* @include-if KEY */ ... /* @end */`                 | If `KEY` is active                                                                                     |
| **Exclude Block**                | `/* @exclude-if KEY */ ... /* @end */`                 | If `KEY` is **not** active                                                                             |
| **Include Block with Else**      | `/* @include-if KEY */ ... /* @else */ ... /* @end */` | First block if `KEY` is active, `@else` block if not                                                   |
| **Exclude Block with Else**      | `/* @exclude-if KEY */ ... /* @else */ ... /* @end */` | First block if `KEY` is **not** active, `@else` block if active                                        |
| **Inline Include**               | `/* @include-if KEY ... @end */`                       | If `KEY` is active (inside a single comment)                                                           |
| **Inline Exclude**               | `/* @exclude-if KEY ... @end */`                       | If `KEY` is **not** active (inside a single comment)                                                   |
| **Empty Output If Match**        | `/* @file-exclude-if KEY */` | Entire output is **empty** if `KEY` matches. If `DEFAULT` is used, matches when no keys are active     |
| **Javadoc HTML Include/Exclude** | `<!-- @include-if KEY --> ... <!-- @end -->`           | Same as above, but inside Javadoc                                                                      |
| **License Banner**               | `/* @license */`                                       | Replaced with license text from the license map: uses the **active key** if set, otherwise **DEFAULT** |
| **Auto-Add License Header**      | *(no directive in source)*                             | If enabled, automatically inserts license text for the active key at the top of the file               |

### Spanning Directives

These directives mark a block of code that spans multiple lines and ends with a separate `@end` comment.

*   **Syntax**:
    ```java
    /* @include-if KEY */
    // Code to include if KEY is active
    /* @end */
    /* @exclude-if KEY */
    // Code to exclude if KEY is active
    /* @end */
    ```

*   **Behavior**:
    *   If `@include-if KEY` is active: The entire block between `/* @include-if KEY */` and `/* @end */` (inclusive of newlines, exclusive of the directive comments themselves) is preserved.
    *   If `@include-if KEY` is not active: The entire block, including the directive comments, is removed.
    *   If `@exclude-if KEY` is active: The entire block between `/* @exclude-if KEY */` and `/* @end */` (inclusive of newlines, exclusive of the directive comments themselves) is removed.
    *   If `@exclude-if KEY` is not active: The entire block between `/* @exclude-if KEY */` and `/* @end */` (inclusive of newlines, exclusive of the directive comments themselves) is preserved.

### `@else` Directive

You can pair `@include-if` or `@exclude-if` with an optional `@else` block to provide an alternate code path when the condition is not met.

**Syntax:**
```java
/* @include-if KEY */
Code if KEY is active
/* @else */
Code if KEY is not active
/* @end */

/* @exclude-if KEY */
Code if KEY is not active
/* @else */
Code if KEY is active
/* @end */

### Single-Comment Include

This directive allows including content directly within a single block comment, terminated by an `@end` atom within the same comment.

*   **Syntax**:
    ```java
    /* @include-if KEY|KEY2|KEY3 code to include @end */
    /* @exclude-if KEY|KEY2|KEY3 code to include @end */
    ```

*   **Behavior**:
    *   If `@include-if KEY` is active: The content between `KEY` and `@end` within the single comment is extracted and emitted. The entire original comment is removed.
    *   If `@include-if KEY` is not active: then entire comment is omitted.
    *   One or more keys may be supplied delimited by a pipe (Must be no spaces either side of the pipe.
    *   The value of the code to include is supplied as written, but one space either side will be stripped if present.

### HTML Comments in Javadoc

Directives can also be nested within Javadoc's HTML comments.

*   **Syntax**:
    ```java
    /**
     * Javadoc content.
     * <!-- @include-if KEY -->
     *   Content to include if KEY is active.
     * <!-- @end -->
     * More Javadoc content.
     */
    ```
    or
    ```java
    /**
     * Javadoc content.
     * <!-- @exclude-if KEY -->
     *   Content to exclude if KEY is active.
     * <!-- @end -->
     * More Javadoc content.
     */
    ```

*   **Behavior**: Similar to spanning directives, but within the Javadoc context.

### `@license` Directive

The `@license` directive is used to automatically insert a license header into your source code at build time.

**Syntax:**
```java
/* @license */
```

**Behavior:**
- When PicoPica encounters `/* @license */`, it replaces it with the license text from the provided license map.
- You must provide a license map via `.licenseMap(Map<String, String>)`.
- You can specify which license to use by providing keys via `.keys(...)`.
- If no keys are provided, PicoPica uses the `DEFAULT` license (`PicoPica.DEFAULT_LICENSE_KEY`).
- If multiple keys are provided while license mode is enabled, PicoPica throws an error to avoid ambiguity.
- If a required license key is missing from the license map, PicoPica throws an error.
- PicoPica reserves `DEFAULT` as a key to represent the absense of other keys.

**Example:**
```java
String src = "/* @license */\n\npublic class Hello {}";

String result = 
    PicoPica.input(src)
    .keys("OPENSOURCE")
    .licenseMap(Map.of(
        "OPENSOURCE",                 "/* Apache V2 License */",
        PicoPica.DEFAULT_LICENSE_KEY, "/* Premium License */"
    ))
    .exec()
;

// /* Apache V2 License */
// public class Hello {}
```

**Error Cases:**
| Condition | Exception | Message |
|-----------|-----------|---------|
| No license map provided | `IllegalStateException` | `Cannot use @license directive unless one or more license files has been provided.` |
| Multiple keys provided in license mode | `IllegalArgumentException` | `Cannot provide multiple keys if license mode is enabled (would lead to ambiguity).` |
| Missing license for a key | `IllegalStateException` | `Cannot find license for 'KEY' key.` |

---

**Example with Default License:**
```java
String src = "/* @license */\n\npublic class Hello {}";

String result = 
    PicoPica.input(src)
    .licenseMap(Map.of(PicoPica.DEFAULT_LICENSE_KEY, "/* Premium License */"))
    .exec()
;

// /* Premium License */
// public class Hello {}
```

## 📜 Auto-Add License Header

Normally, PicoPica only injects a license when it sees a `/* @license */` directive in your source.
**Auto-Add License Header mode** lets you skip the directive — PicoPica will automatically prepend the chosen license text to the file.

### How it works

* If the source contains **no** `/* @license */` directive, PicoPica inserts the resolved license at the very start of the file.
* The license is chosen exactly the same way as in [`@license`](#📜-license-injection-license) mode:

  * **If a key is active** → use the license text for that key.
  * **If no key is active** → use the `DEFAULT` license text.
* The license text should typically end with a newline (`\n`) so that your code stays properly formatted.

---

### Examples

**1️⃣ Auto-add with a specific key**

```java
String src = "public class Hello {}";

String result = PicoPica.input(src)
    .keys("OPENSOURCE")
    .licenseMap(Map.of(
        "OPENSOURCE", "/* Apache V2 license */\n",
        PicoPica.DEFAULT_LICENSE_KEY, "/* Premium license */\n"
    ))
    .autoAddLicenseToHeader()
    .exec();

// /* Apache V2 license */
// public class Hello {}
```

---



## 📚 Case Study

Below is a complete example showing how different feature flags affect the output.

---

### 1️⃣ Input Code (with Conditional Markers)

```java
// MyClass.java
package com.example;

public class MyClass {

    public void commonFeature() {
        System.out.println("This is always here.");
    }

    /* @include-if PREMIUM_FEATURE */
    public void premiumFeature() {
        System.out.println("This is only for premium users.");
    }
    /* @end */

    /* @exclude-if BETA_FEATURE */
    public void stableFeature() {
        System.out.println("This is for stable releases.");
    }
    /* @end */

    /**
     * Javadoc for a method.
     * <!-- @include-if PRO_DOCS -->
     *
     * This Javadoc content is only for professional documentation.
     * <!-- @end -->
     *
     * And this part is always visible.
     */
    public void anotherFeature() {
        System.out.println("Another feature.");
    }
}
```

---

### 2️⃣ Feature Flags Overview

| Feature Flag       | Effect |
|--------------------|--------|
| `PREMIUM_FEATURE`  | Adds `premiumFeature()` method |
| `BETA_FEATURE`     | Removes `stableFeature()` method |
| `PRO_DOCS`         | Adds extra Javadoc lines in `anotherFeature()` |

---

### 3️⃣ Output Changes by Feature Combination

Below, `+` means a line is **added**, `-` means a line is **removed**.

---

#### **No Features Active**
```diff
- public void premiumFeature() {
-     System.out.println("This is only for premium users.");
- }
```

---

#### **PREMIUM_FEATURE**
```diff
+ public void premiumFeature() {
+     System.out.println("This is only for premium users.");
+ }
```

---

#### **BETA_FEATURE**
```diff
- public void stableFeature() {
-     System.out.println("This is for stable releases.");
- }
```

---

#### **PRO_DOCS**
```diff
+  * 
+  * This Javadoc content is only for professional documentation.
```

---

#### **PREMIUM_FEATURE + PRO_DOCS**
```diff
+ public void premiumFeature() {
+     System.out.println("This is only for premium users.");
+ }
```

```diff
+  * 
+  * This Javadoc content is only for professional documentation.
```

---

#### **PREMIUM_FEATURE + BETA_FEATURE**
```diff
+ public void premiumFeature() {
+     System.out.println("This is only for premium users.");
+ }
- public void stableFeature() {
-     System.out.println("This is for stable releases.");
- }
```

---

#### **BETA_FEATURE + PRO_DOCS**
```diff
- public void stableFeature() {
-     System.out.println("This is for stable releases.");
- }
```

```diff
+  * 
+  * This Javadoc content is only for professional documentation.
```

---

#### **All Features Active**
```diff
+ public void premiumFeature() {
+     System.out.println("This is only for premium users.");
+ }
- public void stableFeature() {
-     System.out.println("This is for stable releases.");
- }
```

```diff
+  * 
+  * This Javadoc content is only for professional documentation.
```
---

### 4️⃣ Full Example Output — **PREMIUM_FEATURE + PRO_DOCS**

```java
package com.example;

public class MyClass {

    public void commonFeature() {
        System.out.println("This is always here.");
    }

    public void premiumFeature() {
        System.out.println("This is only for premium users.");
    }

    public void stableFeature() {
        System.out.println("This is for stable releases.");
    }

    /**
     * Javadoc for a method.
     *
     * This Javadoc content is only for professional documentation.
     *
     * And this part is always visible.
     */
    public void anotherFeature() {
        System.out.println("Another feature.");
    }
}
```




## 📇 Documentation

The primary entry point for PicoPica is the `PicoPica` class.

```java

    import org.ainslec.picopica.PicoPica;
    // ... other imports 

    // Code snippets start here

    // Regular @include-if
    {
        String input = "/* @include-if FOO */A/* @end */";
        assertEquals("", PicoPica.exec(input));
        assertEquals("A",  PicoPica.exec(input, "FOO"));
    }

    // Regular @exclude-if
    {
        String input = "/* @exclude-if FOO */A/* @end */";
        assertEquals("A", PicoPica.exec(input));
        assertEquals("",  PicoPica.exec(input, "FOO"));
    }

    // Multi line @include-if and @exclude-if
    {
        String input = """
            /* @include-if FOO */
            // Code to include if FOO is supplied
            /* @end */
            /* @exclude-if FOO */
            // Code to exclude if FOO is supplied
            /* @end */""";
        
        // Lines with only whitespaces and directives are removed (whether evaluated true or false)
        assertEquals("// Code to exclude if FOO is supplied\n", PicoPica.exec(input));

        // Lines with only whitespaces and directives are removed (whether evaluated true or false)
        assertEquals("// Code to include if FOO is supplied\n", PicoPica.exec(input, "FOO"));
    }

    // Inline @include-if
    {
        String input = "myMethod(param1/* @include-if FOO , param2 @end*/);";
        assertEquals("myMethod(param1);", PicoPica.exec(input));
        assertEquals("myMethod(param1, param2);", PicoPica.exec(input,"FOO"));
    }

    // Inline @exclude-if
    {
        String input = "/* @exclude-if FOO hello @end */";
        assertEquals("hello", PicoPica.exec(input));
        assertEquals("", PicoPica.exec(input, "FOO"));
    }

    // JavaDoc @include-if
    {
        String input = """
                public class MyClass {
                    /**
                     * Javadoc for a method.
                     * <!-- @include-if FOO -->
                     * 
                     * This Javadoc content is only for professional documentation. 
                     * <!-- @end -->
                     */
                    public void anotherFeature() {
                        System.out.println("Another feature.");
                    }
                }""";

        assertEquals(
            """
            public class MyClass {
                /**
                 * Javadoc for a method.
                 */
                public void anotherFeature() {
                    System.out.println("Another feature.");
                }
            }""", PicoPica.exec(input));

        assertEquals(
            """
            public class MyClass {
                /**
                 * Javadoc for a method.
                 *
                 * This Javadoc content is only for professional documentation.
                 */
                public void anotherFeature() {
                    System.out.println("Another feature.");
                }
            }""", PicoPica.exec(input, "FOO"));
    }

    {
        String input = """
                public class MyClass {
                    /**
                     * Javadoc for a method.
                     * <!-- @exclude-if FOO -->
                     * 
                     * This Javadoc content is only for professional documentation. 
                     * <!-- @end -->
                     */
                    public void anotherFeature() {
                        System.out.println("Another feature.");
                    }
                }""";
        
        assertEquals(
            """
            public class MyClass {
                /**
                 * Javadoc for a method.
                 *
                 * This Javadoc content is only for professional documentation.
                 */
                public void anotherFeature() {
                    System.out.println("Another feature.");
                }
            }""", PicoPica.exec(input));
        
        assertEquals(
            """
            public class MyClass {
                /**
                 * Javadoc for a method.
                 */
                public void anotherFeature() {
                    System.out.println("Another feature.");
                }
            }""", PicoPica.exec(input,"FOO"));
    }

    // Using multiple labels (no spaces should be between | chars)
    {
        String input = "myMethod(param1/* @include-if FOO|BAR , param2 @end*/);";
        assertEquals("myMethod(param1);", PicoPica.exec(input));
        assertEquals("myMethod(param1, param2);", PicoPica.exec(input, "FOO"));
        assertEquals("myMethod(param1, param2);", PicoPica.exec(input,"BAR"));
        assertEquals("myMethod(param1);", PicoPica.exec(input, "CAR"));
    }
```



## 🚀 Maven / Gradle

**Maven:**

```xml
<dependency>
    <groupId>org.ainslec</groupId>
    <artifactId>picopica</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Gradle (Groovy):**

```groovy
dependencies {
    implementation 'org.ainslec:picopica:1.0.0'
}
```

## ❓ FAQS

Waiting for questions ... 