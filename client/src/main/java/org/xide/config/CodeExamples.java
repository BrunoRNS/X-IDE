package org.xide.config;

public final class CodeExamples {

	public static final String PYTHON = "print(\"Hello, World!\")";

	public static final String JAVA = "public class HelloWorld {\n" + "    public static void main(String[] args) {\n"
			+ "        System.out.println(\"Hello, World!\");\n" + "    }\n" + "}   ";

	public static final String JAVASCRIPT = "console.log(\"Hello, World!\");";

	public static final String GROOVY = "println \"Hello, World!\"   ";

	public static final String KOTLIN = "fun main() {\n" + //
			"    println(\"Hello, World!\")\n" + //
			"}   ";

	public static final String CPP = "#include <bits/stdc++.h>\n" + //
			"using namespace std;\n" + //
			"\n" + //
			"int main() {\n" + //
			"    ios::sync_with_stdio(false);\n" + //
			"    cin.tie(nullptr);\n" + //
			"\n" + //
			"    cout << \"Hello, World!\" << '\\n';\n" + //
			"    return 0;\n" + //
			"}   ";

	public static final String C = "#include <stdio.h>\n" + //
			"\n" + //
			"int main(void) {\n" + //
			"    printf(\"Hello, World!\\n\");\n" + //
			"    return 0;\n" + //
			"}   ";

	public static final String CSHARP = "Console.WriteLine(\"Hello, World!\");   ";

	public static final String GO = "package main\n" + //
			"\n" + //
			"import \"fmt\"\n" + //
			"\n" + //
			"func main() {\n" + //
			"    fmt.Println(\"Hello, World!\")\n" + //
			"}   ";

	public static final String RUST = "fn main() {\n" + //
			"    println!(\"Hello, World!\");\n" + //
			"}   ";

}
