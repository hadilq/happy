{ nixpkgs ? import <nixpkgs> {} }:
with nixpkgs.pkgs;
pkgs.mkShell {
  name = "zulu";
  buildInputs = [
    jdk11
  ];

  shellHook = ''
    export JAVA_HOME="${pkgs.jdk11}"
  '';
}

