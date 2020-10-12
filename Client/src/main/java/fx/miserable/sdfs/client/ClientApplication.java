package fx.miserable.sdfs.client;

import fx.miserable.sdfs.client.dto.FileOrDirectoryInformation;
import fx.miserable.sdfs.client.service.NamingNodeConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;
import java.util.stream.Collectors;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class ClientApplication {

	private final NamingNodeConnectionService namingNodeConnectionService;
	private Stack<String> cdir = new Stack<>();

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	@Bean
	public CommandLineRunner run() {
		return (args) -> {
			var sc = new Scanner(System.in);

			while (true) {
				var cmd = sc.nextLine().split(" ");

				switch (cmd[0]) {
					case "cd" -> this.cd(cmd);
					case "ls" -> this.ls(cmd);
					case "touch" -> this.touch(cmd);
					case "pwd" -> this.pwd(cmd);
					case "cp" -> this.cp(cmd);
					case "rm" -> this.rm(cmd);
					case "init" -> this.init(cmd);
					case "info" -> this.info(cmd);
					case "help" -> {
						log.info("Available commands are:");
						log.info("cd <directory> - Change Directory (Even if it does not exist)");
						log.info("ls - List files");
						log.info("pwd - Show current directory");
						log.info("touch <path> - Create new file");
						log.info("cp --from_local <local_path> <sdfs_path> - Upload file from <local_path> on local machine to <sdfs_path> in sdfs");
						log.info("cp <sdfs_path> <local_path> - Copy file from <sdfs_path> to <local_path> on local machine");
						log.info("rm <path> - Remove file specified by path");
						log.info("info <path> - Get Information about file");
						log.info("init - Prune all storage nodes and see available size");
					}
					default -> log.error("Sorry not implemented!");
				}

			}
		};
	}

	private void info(String[] cmd) {
		if (cmd.length < 2) {
			log.error("Incorrect usage.");
			return;
		}

		var path = cmd[1];

		var currPath = String.join("/", this.cdir);
		this.cd(new String[]{"cd", path});
		var fullpath = String.join("/", this.cdir);
		this.cd(new String[]{"cd", "/" + currPath});

		namingNodeConnectionService.info(fullpath);
	}

	private void init(String[] cmd) {
		namingNodeConnectionService.init();
	}

	private void rm(String[] cmd) {
		if (cmd.length < 2) {
			log.error("Incorrect usage.");
			return;
		}

		var path = cmd[1];

		var currPath = String.join("/", this.cdir);
		this.cd(new String[]{"cd", path});
		var fullpath = String.join("/", this.cdir);
		this.cd(new String[]{"cd", "/" + currPath});

		namingNodeConnectionService.deleteFile(fullpath);
		log.info("File deleted");
	}

	private void cp(String[] cmd) {
		if (cmd.length < 3) {
			log.error("Incorrect usage.");
			return;
		}

		if (cmd[1].equals("--from_local")) {
			log.info("Sending file...");
			if (cmd.length < 4) {
				log.error("Incorrect usage.");
				return;
			}

			var file = new File(cmd[2]);
			var path = cmd[3];

			var currPath = String.join("/", this.cdir);
			this.cd(new String[]{"cd", path});
			var fullpath = String.join("/", this.cdir);
			this.cd(new String[]{"cd", "/" + currPath});

			namingNodeConnectionService.uploadFile(file, fullpath);
			log.info("File sent");
		} else {
			log.info("Downloading file...");
			var file = new File(cmd[3]);
			var path = cmd[2];

			var currPath = String.join("/", this.cdir);
			this.cd(new String[]{"cd", path});
			var fullpath = String.join("/", this.cdir);
			this.cd(new String[]{"cd", "/" + currPath});

			namingNodeConnectionService.downloadFile(file, fullpath);
			log.info("File downloaded");
		}
	}

	private void pwd(String[] cmd) {
		log.info(String.join("/", this.cdir));
	}

	private void touch(String[] cmd) {
		if (cmd.length < 2) {
			log.error("Incorrect usage.");
			return;
		}

		var path = cmd[1];
		var currPath = String.join("/", this.cdir);
		this.cd(new String[]{"cd", path});
		var fullpath = String.join("/", this.cdir);
		this.cd(new String[]{"cd", "/" + currPath});

		namingNodeConnectionService.createFile(fullpath);
	}

	private void ls(String[] cmd) {

		var path = String.join("/", this.cdir).equals("") ? "" : String.join("/", this.cdir) + "/";
		var files_dirs = namingNodeConnectionService.getFilesAndFolders(path);

		var dirs = files_dirs.stream().filter(FileOrDirectoryInformation::isDirectory)
							 .collect(Collectors.toList());

		var files = files_dirs.stream().filter(el -> !el.isDirectory())
							  .collect(Collectors.toList());

		dirs.forEach(el -> {
					log.info("Directory {}. # of files : {}. Size : {} KB.", el.getName(), el
							.getFilesCount(), el.getSize().divide(BigInteger.valueOf(1024)));
				}
		);

		files.forEach(el -> {
					log.info("File {}. Size : {}.", el.getName(), el.getSize()
																	.divide(BigInteger.valueOf(1024)));
				}
		);

	}

	private void cd(String[] cmd) {
		if (cmd.length < 2) {
			log.error("Incorrect Usage");
			return;
		}

		try {
			if (cmd[1].charAt(0) == '/') {
				this.cdir = new Stack<>();
				cmd[1] = cmd[1].substring(1);
			}

			var path = Arrays.asList(cmd[1].split("/"));
			path.forEach((el) -> {
				switch (el) {
					case "." -> {}
					case ".." -> this.cdir.pop();
					default -> this.cdir.push(el);
				}
			});

			this.cdir = this.cdir.stream().filter(el -> !el.equals("/"))
								 .collect(Collectors.toCollection(Stack::new));
		} catch (Exception e) {
			log.error("Incorrect usage. Returning to root");
			this.cdir = new Stack<>();
		}
	}
}
