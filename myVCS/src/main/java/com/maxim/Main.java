package com.maxim;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.maxim.vcs_impl.Vcs;
import com.maxim.vcs_impl.VcsImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements console mode
 */
public class Main {
    private final Vcs vcs;
    {
        Vcs vcs1 = null;
        try {
            vcs1 = new VcsImpl(Paths.get("."));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("failed to init vcs");
            System.exit(3);
        }
        this.vcs = vcs1;
    }

    private final String[] args;

    private final List<Command> commands = ImmutableList.of(
            new Command(1, "add", "<path>") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    Path path = Paths.get(parseArgs(args).get(0));
                    vcs.add(path);
                }
            },

            new Command(1, "commit", "message") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    vcs.commit(parseArgs(args).get(0));
                }
            },
            new Command(1, "create-branch", "<branch name>") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    vcs.createBranch(parseArgs(args).get(0));
                }
            },
            new Command(2,"checkout", "-b", "<branch name>") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    vcs.checkoutBranch(parseArgs(args).get(0));
                }
            },
            new Command(2, "checkout", "-c", "<commit id>") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    long arg = Long.parseLong(parseArgs(args).get(0));
                    vcs.checkoutCommit(arg);
                }
            },
            new Command(1,"merge", "<other branch name>") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    vcs.merge(parseArgs(args).get(0));
                }
            },
            new Command(2, "log", "-b") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    System.out.println("---- current branch:");
                    System.out.println(vcs.getCurrentBranchName());
                    System.out.println();
                    System.out.println("---- all branches:");

                    vcs.logBranches().forEach(System.out::println);
                }
            },
            new Command(2, "log", "-c") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    System.out.println("---- current commit:");
                    System.out.println(vcs.getCurrentCommitId());
                    System.out.println();
                    System.out.println("---- all commits:");

                    vcs.logCommits().forEach(System.out::println);
                }
            },
            new Command(1, "clean") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    vcs.clean();
                }
            },
            new Command(1, "status") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    vcs.status()
                            .forEach((key, value) -> System.out.println(key + " " + value));
                }
            },
            new Command(1, "rm", "<path>") {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    Path path = Paths.get(parseArgs(args).get(0));
                    vcs.rm(path);
                }
            },
            new Command(1, "reset", "<path>")  {
                @Override
                public void execute(@NotNull String[] args) throws IOException {
                    Path path = Paths.get(parseArgs(args).get(0));
                    vcs.reset(path);
                }
            }
    );

    public Main(String args[]) {
        this.args = args;
    }

    public static void main(String[] args) {
        new Main(args).run();
    }

    public void run() {
        List<Command> ok_commands = commands.stream().filter(command -> command.check(args)).collect(Collectors.toList());
        if (ok_commands.size() == 1) {
            try {
                Command command = ok_commands.get(0);
                command.execute(args);
            } catch (IOException e){
                e.printStackTrace();
            }
        } else {
            System.out.println("usage: \n");
            String help_message = commands.stream().map(Command::getMessage).collect(Collectors.joining("\n"));
            System.out.println(help_message);
        }
    }

    private abstract static class Command {
        private final ImmutableList<String> args_names;
        private final int prefix_len;

        public Command(int args_count, @NotNull String... arg_names) {
            this.args_names = ImmutableList.copyOf(arg_names);
            this.prefix_len = args_count;
        }

        public String getMessage() {
            return args_names.stream().collect(Collectors.joining(" "));
        }

        public boolean check(@NotNull String[] args) {
            return args_names.size() == args.length &&
                   ImmutableList.copyOf(args).subList(0, prefix_len).equals(args_names.subList(0, prefix_len));
        }

        @Nullable
        public List<String> parseArgs(@NotNull String[] args) {
            return check(args) ? ImmutableList.copyOf(args).subList(prefix_len, args_names.size()) : null;
        }

        public abstract void execute(String[] args) throws IOException;
    }
}
