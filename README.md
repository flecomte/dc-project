# Installation

## On windows
1. Install git 
    - Download and install: https://git-scm.com/download/win
2. Install Make
    - Go to [ezwinports](https://sourceforge.net/projects/ezwinports/files/).
    - Download `make-4.1-2-without-guile-w32-bin.zip` (get the version without guile).
    - Extract zip.
    - Copy the contents to your `Git\mingw64\` merging the folders, but **do NOT overwrite/replace** any existing files.

## Run dockers


```bash
$ make run-docker
```

## Add fixtures
```bash
$ make fixtures
```