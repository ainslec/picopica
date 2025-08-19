package org.ainslec.picopica;

import java.util.HashMap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

public class DetailedJavaTests {

    private static final String JAVADOC_SNIPPET = "/**\n" +
	" * pre\n" +
	" * <!-- @include-if FOO -->\n" +
	" * keep\n" +
	" * <!-- @exclude-if BAR -->\n" +
	" * drop\n" +
	" * <!-- @end -->\n" +
	" * <!-- @end -->\n" +
	" * post\n" +
	" */";
    
    private static final String DEEP_NESTED_HTML = """
            /**
             * a
             * <!-- @include-if K1 -->
             *   i1
             *   <!-- @exclude-if K2 -->
             *     gone-if-k2
             *     <!-- @include-if K3 -->
             *       i3
             *     <!-- @end -->
             *   <!-- @end -->
             *   tail
             * <!-- @end -->
             * z
             */""";


	@Test
    public void stringLiteralsAreNotTokenizedAsCommentsAndBlockExcludeWorks() {
        String src = "class A{String s=\"/* not a comment */\"; /* @exclude-if X */int y;/* @end */}";

        // no keys -> keep inner, directive comments removed
        String out1 = PicoPica.exec(src);
        assertEquals("class A{String s=\"/* not a comment */\"; int y;}", out1);

        // X present -> drop inner, directive comments removed
        String out2 = PicoPica.exec(src, "X");
        assertEquals("class A{String s=\"/* not a comment */\"; }", out2);
    }


    @Test
    public void includeIfKeepsInnerContentSingleSpace() {
        String src = "/* @include-if FOO x @end */";
        // No A -> all inner content kept (directives removed)
        String out1 = PicoPica.exec(src, "FOO");
        assertEquals("x", out1); // This fails - actually returns " x "
    }
    
    @Test
    public void includeIfKeepsInnerContentDoubleSpaceViaFluentApi() {
        String src = "/* @include-if FOO  x @end */";

        // No A -> all inner content kept (directives removed)
        String out1 =  PicoPica.input(src, "FOO").exec();
        assertEquals(" x", out1); // This fails - actually returns "  x "
    }

    @Test
    public void includeIfEmitsMultilineBodyWithTrailingNewline() {
        String src = """
		/* @include-if FOO x
		@end */
        """;

        // No A -> all inner content kept (directives removed)
        String out1 = PicoPica.exec(src, "FOO");
        
        assertEquals("x\n", out1);
        assertEquals("", PicoPica.exec(src));
    }
    
    @Test
    public void includeIfEmitsHelloLineWhenPresent() {
        String src = """
		/* @include-if FOO
		hello
		@end */""";

        // No A -> all inner content kept (directives removed)
        String out1 = PicoPica.exec(src, "FOO");
        
        assertEquals("hello\n", out1);
        assertEquals("", PicoPica.exec(src));
    }
    @Test
    public void includeIfWithinTextEmitsAndOtherwiseDrops() {
        String src = """
		a/* @include-if FOO
		hello
		@end */""";

        // No A -> all inner content kept (directives removed)
        String out1 = PicoPica.exec(src, "FOO");
        
        assertEquals("a\nhello\n", out1);
        assertEquals("a", PicoPica.exec(src));
    }
    @Test
    public void includeIfAbsentDropsBody() {
        String src = "/* @include-if FOO x @end */";


        // A present -> everything inside the outer exclude removed
        String out2 = PicoPica.exec(src);
        assertEquals("", out2);
    }
    
    
    @Test
    public void carriageReturnsAndLinefeedsAreMaintained() {
        String src = "1\r2\r\n3\n\r4\n\n\n";


        // A present -> everything inside the outer exclude removed
        String out2 = PicoPica.exec(src);
        assertEquals(src, out2);
    }
    
    @Test
    public void readmeExamples() {
    	
    	{
            String input = "/* @exclude-if FOO */A/* @end */";
            assertEquals("A", PicoPica.exec(input));
            assertEquals("", PicoPica.exec(input, "FOO"));
    		
    	}
        
    	{
            String input = "/* @exclude-if FOO */A/* @end */";
            assertEquals("A", PicoPica.exec(input));
            assertEquals("", PicoPica.exec(input, "FOO"));
    		
    	}


    	{
    		String input = """
			    /* @include-if FOO */
			    // Code to include if FOO is supplied
			    /* @end */
			    /* @exclude-if FOO */
			    // Code to exclude if FOO is supplied
			    /* @end */""";
            assertEquals("// Code to exclude if FOO is supplied\n", PicoPica.exec(input));
            assertEquals("// Code to include if FOO is supplied\n", PicoPica.exec(input, "FOO"));
    	}
    	
       	{
            String input = "myMethod(param1/* @include-if FOO , param2 @end*/);";
            assertEquals("myMethod(param1);", PicoPica.exec(input));
            assertEquals("myMethod(param1, param2);", PicoPica.exec(input, "FOO"));
    	}
       	
       	
    	{
            String input = "/* @exclude-if FOO hello @end */";
            assertEquals("hello", PicoPica.exec(input));
            assertEquals("", PicoPica.exec(input, "FOO"));
    	}
       	
    	
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
				}""", PicoPica.exec(input, "FOO"));
    	}
    	
    	
    	// Using multiple labels (no spaces should be between | chars)
    	{
    		String input = "myMethod(param1/* @include-if FOO|BAR , param2 @end*/);";
            assertEquals("myMethod(param1);", PicoPica.exec(input));
            assertEquals("myMethod(param1, param2);", PicoPica.exec(input, "FOO"));
            assertEquals("myMethod(param1, param2);", PicoPica.exec(input, "BAR"));
            assertEquals("myMethod(param1);", PicoPica.exec(input,"CAR"));
    	}
    }



    @Test
    public void nestedBlockExcludeRespectsDepth() {
        // Layout: X [exclude A -> Y [exclude A -> Z] W ] T
        String src = "X/* @exclude-if A*/Y/* @exclude-if A */Z/* @end */W/* @end */T";

        // No A -> all inner content kept (directives removed)
        String out1 = PicoPica.exec(src);
        assertEquals("XYZWT", out1);

        // A present -> everything inside the outer exclude removed
        String out2 = PicoPica.exec(src, "A");
        assertEquals("XT", out2);
    }

    @Test
    public void singleCommentIncludeEmitsOnlyWhenKeyPresentOtherwiseDropped() {
        String src = "A/* @include-if FOO B @end */D";

        String out1 = PicoPica.exec(src, "FOO");
        
        assertEquals("ABD", out1);

        String out2 = PicoPica.exec(src);
        assertEquals("AD", out2);
    }

    @Test
    public void javadocHtmlIncludeWithNestedExcludeIsDepthAware1() {
        String src = JAVADOC_SNIPPET;

        // Case 1: FOO present, BAR absent -> include fires; exclude does NOT fire
        String expected1 =
                "/**\n" +
                " * pre\n" +
                " * keep\n" +
                " * drop\n" +
                " * post\n" +
                " */";
        assertEquals(expected1, PicoPica.exec(src, "FOO"));

    }
    
    
    @Test
    public void javadocHtmlIncludeWithNestedExcludeIsDepthAware2() {
        String src = JAVADOC_SNIPPET;


        // Case 2: FOO and BAR present -> include fires; exclude DOES fire
        String expected2 =
                "/**\n" +
                " * pre\n" +
                " * keep\n" +
                " * post\n" +
                " */";
        String actual = PicoPica.exec(src, "FOO", "BAR");
        
		assertEquals(expected2, actual);

 
    }
    
    
    
    @Test
    public void javadocHtmlIncludeWithNestedExcludeIsDepthAware3() {
        String src = JAVADOC_SNIPPET;

        // Case 3: FOO absent -> include does NOT fire; the whole include block is removed
        String expected3 =
                "/**\n" +
                " * pre\n" +
                " * post\n" +
                " */";
        assertEquals(expected3, PicoPica.exec(src));
    }
    

    @Test
    public void unterminatedBlockCommentThrows() {
        String src = "class B { /* @exclude-if X  ";
        assertThrows(IllegalStateException.class, () -> {PicoPica.exec(src);});
    }
    /* 1) Non-directive comments are preserved byte-for-byte */
    @Test
    public void nonDirectiveBlockCommentsArePreservedVerbatim() {
        String src = """
                class X {
                  int a = 1; /* not a directive: @exclude-ifZ */
                  /*  plain block  */
                  int b = 2;
                }
                """;
        String out = PicoPica.exec(src);
        assertEquals(src, out);
    }

    /* 2) @include-if with missing @end should throw */
    @Test
    public void includeMissingEndThrows() {
        String src = """
                A/* @include-if FOO  body without end */Z
                """;
        // Note: there's no "@end" token inside the comment body.
        assertThrows(IllegalStateException.class, () -> {PicoPica.exec(src, "FOO");});
    }

    /* 3) @include-if with no key should throw */
    @Test
    public void includeMissingKeyThrows() {
        String src = """
                A/* @include-if    @end */Z
                """;
        assertThrows(IllegalStateException.class, () -> PicoPica.exec(src, "ANY"));
    }

	/* 4) Unclosed @exclude-if should throw */
    @Test
    public void excludeMissingEndThrows() {
        String src = """
                start/* @exclude-if KEY */ middle
                """;
        // No matching /* @end */
        assertThrows(IllegalStateException.class, () -> PicoPica.exec(src, "KEY"));
    }

	/* 5) String with tricky escapes must not produce comment tokens */
    @Test
    public void escapedQuotesInsideStringDoNotStartComments() {
        String src = """
                class S {
                  String s = "slash star \\/* still string *\\/ end";
                  /* @exclude-if X */int z;/* @end */
                }
                """;
        String out1 = PicoPica.exec(src);
        assertTrue(out1.contains("int z;"));
        String out2 = PicoPica.exec(src, "X");
        assertFalse(out2.contains("int z;"));
        assertTrue(out2.contains("String s = \"slash star \\/* still string *\\/ end\";"));
    }


    /* 6)  */
    @Test
    public void pipeSeparatedTokens() {
    	
        String src = """
                A/* @include-if FOO|EXTRA --ignored-- @end */B""";

        String out1 = PicoPica.exec(src, "FOO");
        assertEquals("A--ignored--B", out1);
        
        String out2 = PicoPica.exec(src, "EXTRA");
        assertEquals("A--ignored--B", out2);
 
        String out3 = PicoPica.exec(src, "FOO,EXTRA");
        assertEquals("AB", out3);
    }


    
    /* 7) @include-if inner body is recursively processed */
    @Test
    public void includeEmitsInnerTextWhenKeyPresentOtherwiseDropped()  {
        String src = """
                P/* @include-if FOO
                Q
                R S
                @end */T
                """;

        String out1 = PicoPica.exec(src, "FOO");
        assertEquals("P\nQ\nR S\nT\n", out1); // note the trailing \n

        String out2 = PicoPica.exec(src);
        assertEquals("PT\n", out2);
    }

    
    @Test
    public void includeEmitsInnerTextWhenKeyPresentOtherwiseDroppedInline()  {
        String src = "P/* @include-if FOO Q @end */T";

        String out1 = PicoPica.exec(src, "FOO");
        assertEquals("PQT", out1); // note the trailing \n

        String out2 = PicoPica.exec(src);
        assertEquals("PT", out2);
    }
    
    /* 8) EXCLUDE span keeps processing inner includes when exclude DOES NOT trigger */
    @Test
    public void excludeSpanProcessesInnerIncludesWhenNotTriggered() {
        String src = """
                A/* @exclude-if K */ M/* @include-if FOO x @end */N /* @end */Z""";

        // K absent, FOO present -> exclude not triggered, inner include should fire
        String out1 = PicoPica.exec(src, "FOO");
        assertEquals("A MxN Z", out1);

        // K present -> everything inside outer exclude removed regardless of inner include
        String out2 = PicoPica.exec(src, "K", "FOO");
        assertEquals("AZ", out2);
    }



    
    /* 9) EXCLUDE region can contain */
    @Test
    public void excludeRegionMayContainStarSlashInText() {
        String src = """
                L/* @exclude-if X */ contains */ inside text and more /* @end */R""";

        // X present -> drop the whole middle
        String out1 = PicoPica.exec(src, "X");
        assertEquals("LR", out1);


        // X absent -> content is kept verbatim (minus the directive comments)
        String out2 = PicoPica.exec(src);
        assertEquals("L contains */ inside text and more R", out2);
    }

    /* 10) Multiple sibling excludes handled independently with depth accounting */
    @Test
    public void multipleSiblingExcludes() {
        String src = """
                A/* @exclude-if AKEY */X/* @end */B/* @exclude-if BKEY */Y/* @end */C""";

        assertEquals("AXBYC", PicoPica.exec(src));
        assertEquals("ABYC", PicoPica.exec(src, "AKEY"));
        assertEquals("AXBC", PicoPica.exec(src, "BKEY"));
        assertEquals("ABC", PicoPica.exec(src, "AKEY", "BKEY"));
    }



    @Test
    public void numericKeyHandling() {
        String src = """
                A/* @exclude-if AKEY1 */X/* @end */B/* @exclude-if BKEY */Y/* @end */C""";

        assertEquals("AXBYC",PicoPica.exec(src));
        assertEquals("ABYC", PicoPica.exec(src, "AKEY1"));
        assertEquals("AXBC", PicoPica.exec(src, "BKEY"));
        assertEquals("ABC",  PicoPica.exec(src, "AKEY1", "BKEY"));
    }

    /* 11) Javadoc: plain HTML comments are preserved verbatim */
    @Test
    public void javadocPlainHtmlCommentsPreserved() {
        String src = """
                /**
                 * hello
                 * <!-- just a note -->
                 * world
                 */
                """;
        String out = PicoPica.exec(src);
        assertEquals(src, out);
    }

    /* 12) Javadoc HTML @include-if missing @end throws */
    @Test
    public void javadocIncludeMissingEndThrows() {
        String src = """
                /**
                 * before
                 * <!-- @include-if FOO -->
                 * body without end
                 */
                """;
        assertThrows(IllegalStateException.class, () -> {PicoPica.exec(src, "FOO");});
    }

    /* 13) Javadoc HTML @exclude-if missing @end throws (depth scan) */
    @Test
    public void javadocExcludeMissingEndThrows() {
        String src = """
                /**
                 * pre
                 * <!-- @exclude-if BAR -->
                 * mid
                 */
                """;
        assertThrows(IllegalStateException.class, () -> {PicoPica.exec(src, "BAR");});
    }

    /* 14) Javadoc empty form */
    @Test
    public void javadocEmptyIsPreserved() {
        String src = "/**/";
        String out = PicoPica.exec(src);
        assertEquals("/**/", out);
    }
    
    
    @Test
    public void javadocIncludeSingleBlock() {
        String input = """
                /**
                * <!-- @include-if K1 -->
                *   i1
                * <!-- @end -->
                */""";
        
        String expected = """
                /**
                *   i1
                */""";
		String actual = PicoPica.exec(input, "K1");
		assertEquals(expected, actual);
    }
    
    
        /* 15A) Deeply nested HTML: K1 only */
        @Test
        public void javadocDeepNestedHtmlK1Only() {
            String expected = """
                    /**
                     * a
                     *   i1
                     *     gone-if-k2
                     *   tail
                     * z
                     */""";

			String actual = PicoPica.exec(DEEP_NESTED_HTML, "K1");
			assertEquals(expected, actual);
        }
    
        /* 15B) Deeply nested HTML: K1 and K2 */
        @Test
        public void javadocDeepNestedHtmlK1AndK2() {
            String expected = """
                    /**
                     * a
                     *   i1
                     *   tail
                     * z
                     */""";
            assertEquals(expected, PicoPica.exec(DEEP_NESTED_HTML, "K1", "K2"));
        }
    
        /* 15C) Deeply nested HTML: K1 and K3 (K2 absent) */
        @Test
        public void javadocDeepNestedHtmlK1AndK3() {
            String expected = """
                    /**
                     * a
                     *   i1
                     *     gone-if-k2
                     *       i3
                     *   tail
                     * z
                     */""";
            assertEquals(expected, PicoPica.exec(DEEP_NESTED_HTML, "K1", "K3"));
        }
    
        /* 15D) Deeply nested HTML: no K1 (block dropped) */
        @Test
        public void javadocDeepNestedHtmlNoK1() {
            String expected = """
                    /**
                     * a
                     * z
                     */""";
            assertEquals(expected, PicoPica.exec(DEEP_NESTED_HTML));
        }
    
    @Test
    public void javadocWholeBlockIncludedWhenKeyPresent() {
        String src = """
                /* @include-if OPENSOURCE */
                /**
                 * This entire Javadoc appears only if OPENSOURCE is present.
                 *
                 * It will be completely removed otherwise.
                 */
                /* @end */
                /* @exclude-if OPENSOURCE */
                /**
                 * This entire Javadoc appears only if OPENSOURCE is not present.
                 *
                 * It will be completely removed otherwise.
                 */
                /* @end */
                """;
        String expected = """
                /**
                 * This entire Javadoc appears only if OPENSOURCE is present.
                 *
                 * It will be completely removed otherwise.
                 */
                 """;
        

		String actual = PicoPica.exec(src, "OPENSOURCE");
		
		assertEquals(expected, actual);
    }

    @Test
    public void javadocWholeBlockExcludedWhenKeyAbsent() {
        String src = """
                /* @include-if OPENSOURCE */
                /**
                 * This entire Javadoc appears only if OPENSOURCE is present.
                 *
                 * It will be completely removed otherwise.
                 */
                /* @end */
                /* @exclude-if OPENSOURCE */
                /**
                 * This entire Javadoc appears only if OPENSOURCE is not present.
                 *
                 * It will be completely removed otherwise.
                 */
                /* @end */""";
        String expected = """
                /**
                 * This entire Javadoc appears only if OPENSOURCE is not present.
                 *
                 * It will be completely removed otherwise.
                 */
                """;
        assertEquals(expected, PicoPica.exec(src));
    }
    
    
    
    @Test
    public void excludeBlockEmitsBodyWhenNoKey() {
        String src = """
                /* @exclude-if OPENSOURCE */
                b
                c
                d/* @end */""";

		String actual = PicoPica.exec(src); // actual is \nb\n
        String expected = "b\nc\nd";
        
		assertEquals(expected, actual);
    }
    
    @Test
    public void includeBlockEmitsWhenKeyPresent() {
        String src = """
                /* @include-if OPENSOURCE */
                a
                /* @end */""";
        String actual = PicoPica.exec(src, "OPENSOURCE"); // actual is \na\n
        String expected = "a\n";
		assertEquals(expected, actual);
    }
    
    
    @Test
    public void licenseDirectiveScenarios() {
    	
 
    	
    	{
	        String src = "/* @license */\n\npublic class Hello {}";
	        
	        HashMap<String, String> licenseMap = new HashMap<>();
	        licenseMap.put(PicoPica.DEFAULT_LICENSE_KEY, "/* Default license */");
	        
	        // We might relax this rule later.
	        Executable executable = () -> PicoPica.input(src).keys("OPENSOURCEV1", "OPENSOURCEV2").licenseMap(licenseMap).exec();
			IllegalArgumentException retVal = assertThrowsExactly(IllegalArgumentException.class, executable);
			String actualMessage = retVal.getMessage();
			assertEquals("Cannot provide multiple keys if license mode is enabled (would lead to ambiguity).", actualMessage);
    	}
    	
    	
       	{
	        String src = "/* @license */\n\npublic class Hello {}";
	        IllegalStateException retVal = assertThrowsExactly(IllegalStateException.class, () -> PicoPica.input(src).exec());
	        String actualMessage = retVal.getMessage();
	        assertEquals("Cannot use @license directive unless one or more license files has been provided.", actualMessage);
    	}


    	
    	{
	        String src = "/* @license */\n\npublic class Hello {}";
	        
	        HashMap<String, String> licenseMap = new HashMap<>();
	        licenseMap.put(PicoPica.DEFAULT_LICENSE_KEY, "/* Premium license */");
	        
	        // We might relax this rule later.
	        Executable executable = () -> PicoPica.input(src).keys("OPENSOURCE").licenseMap(licenseMap).exec();
	        IllegalStateException retVal = assertThrowsExactly(IllegalStateException.class, executable);
			String actualMessage = retVal.getMessage();
			assertEquals("Cannot find license for 'OPENSOURCE' key.", actualMessage);
    	}
    	
    	{
	        String src = "/* @license */\n\npublic class Hello {}";
	        
	        HashMap<String, String> licenseMap = new HashMap<>();
	        licenseMap.put("OPENSOURCE", "/* Apache V2 license */");
	        
	        // We might relax this rule later.
	        Executable executable = () -> PicoPica.input(src).keys().licenseMap(licenseMap).exec();
	        IllegalStateException retVal = assertThrowsExactly(IllegalStateException.class, executable);
			String actualMessage = retVal.getMessage();
			assertEquals("Cannot find license for 'DEFAULT' key.", actualMessage);
    	}
    	
    	
    	{
	        String src = "/* @license */\n\npublic class Hello {}";
	        
	        HashMap<String, String> licenseMap = new HashMap<>();
	        licenseMap.put("OPENSOURCE", "/* Apache V2 license */\n");
	        licenseMap.put(PicoPica.DEFAULT_LICENSE_KEY, "/* Premium license */\n");
	        
	        // We might relax this rule later.
	        String actual = PicoPica.input(src).keys("OPENSOURCE").licenseMap(licenseMap).exec();
			assertEquals("/* Apache V2 license */\n\npublic class Hello {}", actual);
    	}

    	
    	{
	        String src = "/* @license */\n\npublic class Hello {}";
	        
	        HashMap<String, String> licenseMap = new HashMap<>();
	        licenseMap.put("OPENSOURCE", "/* Apache V2 license */\n");
	        licenseMap.put(PicoPica.DEFAULT_LICENSE_KEY, "/* Premium license */\n");
	        
	        // We might relax this rule later.
	        String actual = PicoPica.input(src).keys().licenseMap(licenseMap).exec();
			assertEquals("/* Premium license */\n\npublic class Hello {}", actual);
    	}
    	
    	{
	        String src = "public class Hello {}";
	        
	        HashMap<String, String> licenseMap = new HashMap<>();
	        licenseMap.put("OPENSOURCE", "/* Apache V2 license */\n");
	        licenseMap.put(PicoPica.DEFAULT_LICENSE_KEY, "/* Premium license */\n");
	        
	        // We might relax this rule later.
	        String actual = PicoPica.input(src).keys("OPENSOURCE").licenseMap(licenseMap).autoAddLicenseToHeader().exec();
			assertEquals("/* Apache V2 license */\npublic class Hello {}", actual);
    	}

    	
    	{
	        String src = "public class Hello {}";
	        
	        HashMap<String, String> licenseMap = new HashMap<>();
	        licenseMap.put("OPENSOURCE", "/* Apache V2 license */\n");
	        licenseMap.put(PicoPica.DEFAULT_LICENSE_KEY, "/* Premium license */\n");
	        
	        // We might relax this rule later.
	        String actual = PicoPica.input(src).keys().licenseMap(licenseMap).autoAddLicenseToHeader().exec();
			assertEquals("/* Premium license */\npublic class Hello {}", actual);
    	}
    	
    	{
	        String src = "/* @license */\npublic class Hello {}";
	        
	        HashMap<String, String> licenseMap = new HashMap<>();
	        licenseMap.put("OPENSOURCE", "/* Apache V2 license */\n");
	        licenseMap.put(PicoPica.DEFAULT_LICENSE_KEY, "/* Premium license */\n");
	        
	        // We might relax this rule later.
	        String actual = PicoPica.input(src).keys().licenseMap(licenseMap).autoAddLicenseToHeader().exec();
			assertEquals("/* Premium license */\n" + "/* Premium license */\n" + "public class Hello {}", actual);
    	}

    }

    
    @Test
    void detectDefaultKeyAsInvalid() {
		IllegalArgumentException retVal = assertThrowsExactly(IllegalArgumentException.class, () -> PicoPica.input("abc").keys("DEFAULT").exec());
		String actualMessage = retVal.getMessage();
		assertEquals("Not allowed to call a key DEFAULT (reserved token name), reserved for absense of other keys.", actualMessage);
    }
    
    @Test
    void picksWithoutKeys() {
    	String SOURCE = "/* @exclude-if ANYTHING */\na\n/* @end */\nb";
        String expected = "a\nb"; 
        
        // The \n from the end of the @end row should be omitted as its a directive only row 
        
        String actual = PicoPica.exec(SOURCE);
		assertEquals(expected, actual);
    }
    
    
    @Test
    void demoOfBug() {
    	String SOURCE = 
"""
{
  /* @include-if PREMIUM_FEATURE */
a
/* @end */
     /* @exclude-if BETA_FEATURE */
b
/* @end */
c
}""";
    	// Leading whitespace on lines with directives should not be emitted, but it is being emitted 
        String expected = """
{
c
}""";
        
        
        String actual = PicoPica.exec(SOURCE, "BETA_FEATURE");
		assertEquals(expected, actual);
    }
    



    @Test
    void excludeElseBlockWhenNotMatchTakesFirstPart() {
        String src = """
            /* @exclude-if BAR */
            KEEP
            /* @else */
            DROP
            /* @end */
            """;
        String expected = """
            KEEP
            """;
        assertEquals(expected, PicoPica.exec(src));
    }

    @Test
    void includeElseInJavadocHtml() {
        String src = """
            /**
             * before
             * <!-- @include-if DOC -->
             * ONE
             * <!-- @else -->
             * TWO
             * <!-- @end -->
             * after
             */
            """;
        String whenMatch = """
            /**
             * before
             * ONE
             * after
             */
            """;
        String whenNoMatch = """
            /**
             * before
             * TWO
             * after
             */
            """;
        assertEquals(whenMatch, PicoPica.exec(src,"DOC"));
        assertEquals(whenNoMatch, PicoPica.exec(src));
    }

    @Test
    void excludeElseInJavadocHtml() {
        String src = """
            /**
             * top
             * <!-- @exclude-if DOC -->
             * FIRST
             * <!-- @else -->
             * SECOND
             * <!-- @end -->
             * end
             */
            """;
        String whenMatch = """
            /**
             * top
             * SECOND
             * end
             */
            """;
        String whenNoMatch = """
            /**
             * top
             * FIRST
             * end
             */
            """;
        assertEquals(whenMatch, PicoPica.exec(src, "DOC"));
        assertEquals(whenNoMatch, PicoPica.exec(src));
    }
    @Test
    void includeElseBlockWhenMatchTakesFirstPart() {
        String src = """
            A
            /* @include-if FOO */
            ONE
            /* @else */
            TWO
            /* @end */
            Z
            """;
        String expected = """
            A
            ONE
            Z
            """;
        assertEquals(expected, PicoPica.exec(src, "FOO"));
    }
    
    @Test
    void simple() {
        String src = """
            /* @include-if FOO */
            ONE
            /* @end */""";
        String expected = "ONE\n";
        String expected2 = "";
        assertEquals(expected, PicoPica.exec(src, "FOO"));
        assertEquals(expected2, PicoPica.exec(src));
    }

    @Test
    void includeElseBlockWhenNotMatchTakesElsePart() {
        String src = """
            pre
            /* @include-if FOO */
            MATCH
            /* @else */
            NOMATCH
            /* @end */
            post
            """;
        String expected = """
            pre
            NOMATCH
            post
            """;
        assertEquals(expected,PicoPica.exec(src));
    }

    @Test
    void excludeElseBlockWhenMatchTakesElsePart() {
        String src = """
            /* @exclude-if BAR */
            KEEP
            /* @else */
            DROP
            /* @end */
            """;
        String expected = """
            DROP
            """;
        assertEquals(expected, PicoPica.exec(src, "BAR"));
    }



    

    @Test
    void fileExcludeIfWhenKeyMatchesEmitsEmpty() {
        String input = """
            /* @file-exclude-if FOO */
            public class Hello {
                void greet() {}
            }
            """;

        String expected = "";

        assertEquals(expected, PicoPica.exec(input, "FOO"));
    }

    @Test
    void fileExcludeIfWhenKeyDoesNotMatchKeepsFile() {
        String input = """
            /* @file-exclude-if FOO */
            public class Hello {
                void greet() {}
            }
            """;

        String expected = """
            public class Hello {
                void greet() {}
            }
            """;

        assertEquals(expected, PicoPica.exec(input, "BAR"));
    }

    @Test
    void fileExcludeIfDefaultWithNoKeysEmitsEmpty() {
        String input = """
            /* @file-exclude-if DEFAULT */
            public class Hello {
                void greet() {}
            }
            """;

        String expected = "";

        assertEquals(expected, PicoPica.exec(input));
    }

    @Test
    void fileExcludeIfDefaultWithKeysProvidedKeepsFile() {
        String input = """
            /* @file-exclude-if DEFAULT */
            public class Hello {
                void greet() {}
            }
            """;

        String expected = """
            public class Hello {
                void greet() {}
            }
            """;

        assertEquals(expected, PicoPica.exec(input, "SOMEKEY"));
    }

    @Test
    void fileExcludeIfWithMultipleKeys() {
        String input = """
            /* @file-exclude-if FOO|BAR */
            public class Hello {
                void greet() {}
            }
            """;

        String expectedWhenFOO = """
            """;

        String expectedWhenZED = """
            public class Hello {
                void greet() {}
            }
            """;

        assertEquals(expectedWhenFOO, PicoPica.exec(input, "FOO"));
        assertEquals(expectedWhenFOO, PicoPica.exec(input, "BAR"));
        assertEquals(expectedWhenZED, PicoPica.exec(input, "ZED"));
    }
    
    @Test
    void inlineIncludeWithMultilineBody() {
        String input = """
            public class Demo {
                /* @include-if FOO
                System.out.println("Hello");
                System.out.println("World");
                @end */
            }
            """;

        String expectedWhenIncluded = """
            public class Demo {
                System.out.println("Hello");
                System.out.println("World");
            }
            """;

        String expectedWhenExcluded = """
            public class Demo {
            }
            """;

        // Run PicoPica with "FOO" key so it should keep the block
        String outputIncluded = PicoPica.exec(input, "FOO");
        assertEquals(expectedWhenIncluded, outputIncluded); 
        
        // Run PicoPica without the "FOO" key so it should remove the block
        String outputExcluded = PicoPica.exec(input);
        assertEquals(expectedWhenExcluded, outputExcluded);
    }
    
    
    @Test
    void includeElseBlockWhenMatchesTakesFirstPart() {
        String src = """
            A
            /* @include-if FOO */
            ONE
            /* @else */
            TWO
            /* @end */
            Z
            """;
        String expected = """
            A
            ONE
            Z
            """;
        assertEquals(expected, PicoPica.exec(src, "FOO"));
    }


}
