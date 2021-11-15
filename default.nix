{ nixpkgs ? import <nixpkgs> {} }:
with nixpkgs.pkgs;
stdenv.mkDerivation {
  name = "zulu";
  buildInputs = [
    jdk11
  ];
}

