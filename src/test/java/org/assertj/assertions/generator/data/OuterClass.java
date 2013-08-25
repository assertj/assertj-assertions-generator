package org.assertj.assertions.generator.data;

public class OuterClass {
    public static class StaticNestedPerson {
        private String name;

        public String getName() {
            return name;
        }

        public static class SNP_StaticNestedPerson {
            private String name;

            public String getName() {
                return name;
            }
        }

        public class SNP_InnerPerson {
            private String name;

            public String getName() {
                return name;
            }
        }
    }

    public class InnerPerson {
        private String name;

        public String getName() {
            return name;
        }

        // static class under inner class is not allowed by java language specifications
        // (public static class IP_StaticNestedPerson)

        public class IP_InnerPerson {
            private String name;

            public String getName() {
                return name;
            }
        }
    }
}
