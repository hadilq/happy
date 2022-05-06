{ nixpkgs ? import <nixpkgs> {} }:
with nixpkgs.pkgs;
pkgs.mkShell {
  name = "happy";
  buildInputs = [
    jdk11
    jetbrains.idea-community
    neovim
  ];

  shellHook = ''
    export JAVA_HOME="${pkgs.jdk11}"
  '';
}

