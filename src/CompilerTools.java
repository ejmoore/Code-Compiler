import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class CompilerTools {
	public boolean compileFiles(Iterable<? extends File> sourcefiles, Iterable<String> options,
			DiagnosticsGobbler<JavaFileObject> diagnostics) {
		boolean success = true;
		for (File sourcefile : sourcefiles) {
			// Doesn't short circuit
			success = compile(sourcefile, options, diagnostics) && success;
		}
		return success;
	}

	public boolean compileString() {
		boolean success = false;

		return success;
	}

	public boolean compile(File sourcefile, Iterable<String> options, DiagnosticsGobbler<JavaFileObject> diagnostics) {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		StandardJavaFileManager manager = compiler.getStandardFileManager(diagnostics, Locale.ENGLISH, null);
		Iterable<? extends JavaFileObject> files = manager
				.getJavaFileObjectsFromFiles(new ArrayList<File>(Arrays.asList(sourcefile)));
		boolean success = false;
		try {
			JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics, options, null, files);
			success = task.call();
		} catch (Exception e) {
			System.out.println("Unexpected Error Trapped During Compilation!");
			e.printStackTrace();
		}
		return success;
	}

	public Iterable<? extends File> makeFileList(String... files) {
		ArrayList<File> list = new ArrayList<File>();
		for (String filename : files) {
			list.add(new File(filename));
		}
		return list;
	}

	public int javac(File workingFolder, String filename, Iterable<String> options, Consumer<String> outConsumer,
			Consumer<String> errConsumer) throws IOException {
		StringJoiner optionsJoiner = new StringJoiner(" ");
		options.forEach(optionsJoiner::add);
		String command = "javac " + optionsJoiner.toString() + " " + filename;
		int result = ShellTools.bashExec(workingFolder, command, 5, TimeUnit.SECONDS, outConsumer, errConsumer);
		return result;
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			System.out.println("Include id as argument");
			return;
		}
		
		Long id = Long.parseLong(args[0]);

		DatabaseTools db = new DatabaseTools();
		db.connectDB();
		File f = db.getFile(id);
		String filename = db.getFileName(id);
		
		System.out.println("Compiling File " + f.exists());

		CompilerTools app = new CompilerTools();

		Iterable<String> options = Arrays.asList("-Xlint:all",
				// "-Xdoclint:all",
				// "-Xprefer:source",
				"-Xmaxerrs", "1000", "-Xmaxwarns", "1000",
				// "-Werror",
				"-deprecation",
				// "-verbose",
				// "-Xdiags:verbose",
				"-g", "-d", ".", "-cp", ".", "-sourcepath", ".");

//		StringJoiner joiner = new StringJoiner("\n");
//		app.javac(new File("./"), filename + ".java", options, (joiner::add), (joiner::add));
//		System.out.println(joiner.toString());

		DiagnosticsGobbler<JavaFileObject> diagnostics = new DiagnosticsGobbler<JavaFileObject>();
		boolean result = app.compileFiles(app.makeFileList(filename), // Returns true if compiled successfully
				options, diagnostics);
		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics // Diagnostics is a list of errors and
																			// warnings
				.getDiagnostics()) {
			System.out.println(diagnostic + "\n");
			db.insertDiagnostic(id, diagnostic.getColumnNumber(), diagnostic.getLineNumber(), diagnostic.getMessage(null));
		}
		
		db.update(id, result);
		db.disconnect();
		
		System.out.println("Compiled: " + result);
		System.out.println("Deleting file: " + new File(filename).delete());
		System.out.println("Deleting class file: " + new File(filename.substring(0, filename.length()-5) + ".class").delete());

	}
}
