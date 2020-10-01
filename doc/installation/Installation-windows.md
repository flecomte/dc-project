Installation on Windows
============

## Prerequisites
### Install chocolaty (optional)
First install Chocolaty => [https://chocolatey.org/install](https://chocolatey.org/install)
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
```

### Install make (optional)
Install make with chocolaty
```powershell
chocolatey install make
```
Install make for gitbash
[https://gist.github.com/evanwill/0207876c3243bbb6863e65ec5dc3f058#make]()

### Install awk (optional)
Install awk with chocolaty
```powershell
chocolatey install awk
```

### Install docker
See: [docs.docker.com/get-docker](https://docs.docker.com/get-docker/)
```powershell
choco install docker-desktop
```

If you use docker with WSL2, you need to change config for elasticsearch:
```powershell
wsl -d docker-desktop
sysctl -w vm.max_map_count=262144
echo "vm.max_map_count = 262144" > /etc/sysctl.d/99-docker-desktop.conf
```
then restart docker-desktop

### Install Git
See: [git-scm.com/downloads](https://git-scm.com/downloads)
```powershell
choco install git
```

### Add JAVA_HOME path
In CMD (Not Powershell)
```cmd
$ setx -m JAVA_HOME "C:\Users\%USERNAME%\.jdks\corretto-11.0.7"
$ setx -m PATH "%PATH%;%JAVA_HOME%\bin";
```