package com.maxim;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.maxim.vcs_impl.Vcs;
import com.maxim.vcs_impl.VcsImpl;
import com.maxim.vcs_objects.VcsCommit;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Main {
    private final Vcs vcs;
    {
        Vcs vcs1 = null;
        try {
            vcs1 = new VcsImpl();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("failed to init vcs");
            System.exit(3);
        }
        this.vcs = vcs1;
    }

    private final String[] args;

    private final List<Command> commands = ImmutableList.of(
            new Command(true,"path", "add") {
                @Override
                public void execute() throws IOException {
                    vcs.add(Paths.get(arg));
                }
            },

            new Command(true,"message", "commit") {
                @Override
                public void execute() throws IOException {
                    vcs.commit(arg);
                }
            },
            new Command(true,"branch_name", "checkout", "-b") {
                @Override
                public void execute() throws IOException {
                    vcs.checkoutBranch(arg);
                }
            },
            new Command(true, "commit_id", "checkout", "-c") {
                @Override
                public void execute() throws IOException {
                    vcs.checkoutCommit(Long.parseLong(arg));
                }
            },
            new Command(true,"other_branch_name", "merge") {
                @Override
                public void execute() throws IOException {
                    vcs.merge(arg);
                }
            },
            new Command(false, "", "log", "-b") {
                @Override
                public void execute() throws IOException {
                    vcs.logBranches();
                }
            },
            new Command(false, "", "log", "-c") {
                @Override
                public void execute() throws IOException {
                    vcs.logCommits();
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
                command.execute();
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
        private final ImmutableList<String> prefix;
        private final String arg_name;
        int args_count;
        protected String arg;

        public Command(boolean has_arg, String arg_name, String... prefix) {
            this.prefix = ImmutableList.copyOf(prefix);
            this.arg_name = arg_name;
            this.args_count = has_arg ? 1 : 0;
        }

        public String getMessage() {
            return prefix.stream().collect(Collectors.joining(" ")) + " " + arg_name;
        }

        public boolean check(String[] args) {
            return prefix.size() + args_count == args.length &&
                   ImmutableList.copyOf(args).subList(0, prefix.size()).equals(prefix);
        }

        public void getArg(String[] args) {
            this.arg = check(args) ? args[args.length - 1] : null;
        }

        public abstract void execute() throws IOException;
    }
}
