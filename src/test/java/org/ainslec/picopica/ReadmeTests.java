package org.ainslec.picopica;

import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for PicoPica using a Java 21 text block as input.
 * We vary active keys across 8 scenarios.
 */
public class ReadmeTests {

    // Java 21 multiline string (text block) of the input source
    private static final String SOURCE = """
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
                
            }""";

    private static String run(Set<String> keys) {
        return PicoPica.input(SOURCE).keys(keys).exec();
    }

    @Test
    void picksWithoutKeys() {
        String expected = """
			// MyClass.java
			package com.example;
			
			public class MyClass {
			
			    public void commonFeature() {
			        System.out.println("This is always here.");
			    }
			
			    public void stableFeature() {
			        System.out.println("This is for stable releases.");
			    }
			
			    /**
			     * Javadoc for a method.
			     *
			     * And this part is always visible.
			     */
			    public void anotherFeature() {
			        System.out.println("Another feature.");
			    }
			
			}""";
        assertEquals(expected, run(Set.of()));
    }

    @Test
    void picksPremiumOnly() {
        String expected = """
			// MyClass.java
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
			     * And this part is always visible.
			     */
			    public void anotherFeature() {
			        System.out.println("Another feature.");
			    }
			    
			}""";
        assertEquals(expected, run(Set.of("PREMIUM_FEATURE")));
    }

    @Test
    void picksBetaOnly() {
        String expected = """
			// MyClass.java
			package com.example;
			
			public class MyClass {
			
			    public void commonFeature() {
			        System.out.println("This is always here.");
			    }
			        
			    /**
			     * Javadoc for a method.
			     *
			     * And this part is always visible.
			     */
			    public void anotherFeature() {
			        System.out.println("Another feature.");
			    }
			
			}""";
        assertEquals(expected, run(Set.of("BETA_FEATURE")));
    }

    @Test
    void picksProDocsOnly() {
        String expected = """
			// MyClass.java
			package com.example;
			
			public class MyClass {
			
			    public void commonFeature() {
			        System.out.println("This is always here.");
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
			    
			}""";
        assertEquals(expected, run(Set.of("PRO_DOCS")));
    }

    @Test
    void picksPremiumAndProDocs() {
        String expected = """
			// MyClass.java
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
			    
			}""";
        assertEquals(expected, run(Set.of("PREMIUM_FEATURE", "PRO_DOCS")));
    }

    @Test
    void picksPremiumAndBeta() {
        String expected = """
			// MyClass.java
			package com.example;
			
			public class MyClass {
			
			    public void commonFeature() {
			        System.out.println("This is always here.");
			    }
			
			    public void premiumFeature() {
			        System.out.println("This is only for premium users.");
			    }
			    
			    /**
			     * Javadoc for a method.
			     *
			     * And this part is always visible.
			     */
			    public void anotherFeature() {
			        System.out.println("Another feature.");
			    }
			
			}""";
        assertEquals(expected, run(Set.of("PREMIUM_FEATURE", "BETA_FEATURE")));
    }

    @Test
    void picksBetaAndProDocs() {
        String expected = """
// MyClass.java
package com.example;

public class MyClass {

    public void commonFeature() {
        System.out.println("This is always here.");
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

}""";
        assertEquals(expected, run(Set.of("BETA_FEATURE", "PRO_DOCS")));
    }

    @Test
    void picksAllThree() {
        String expected = """
			// MyClass.java
			package com.example;
			
			public class MyClass {
			
			    public void commonFeature() {
			        System.out.println("This is always here.");
			    }
			
			    public void premiumFeature() {
			        System.out.println("This is only for premium users.");
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
			
			}""";
        assertEquals(expected, run(Set.of("PREMIUM_FEATURE", "BETA_FEATURE", "PRO_DOCS")));
    }
}


