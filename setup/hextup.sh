#!/bin/sh

answer=""

prompt() {
  echo "$2 (1) Default: $3, (2) Choose"
  read -r option
  if [ "$option" = "1" ]; then
    answer="$3"
  elif [ "$option" = "2" ]; then
    echo "$2: "
    read -r answer
  else
    echo "Invalid option. Exiting"
    exit 1
  fi
}

get_jar() {
  wget "https://oss.sonatype.org/service/local/artifact/maven/redirect?r=snapshots&g=com.github.nkb03&a=$1&v=$version&t=jar" -O "$hextant_home/plugins/$2"
}

check_java() {
  if java --version; then
    echo "Please make sure that your Java JRE has version 11."
  else
    echo "java command is not on your PATH. Exiting"
    exit 1
  fi
}

setup_javafx_jdk() {
  echo "Do you have the JavaFX SDK installed? (1) Yes, (2) No"
  read -r option
  if [ "$option" = "1" ]; then
    javafx_sdk="$HOME/lib/javafx-sdk"
    if [ ! -f "$javafx_sdk/lib/javafx.controls.jar" ]; then
      echo "Where is your SDK located?"
      read -r javafx_sdk
      if [ ! -f "$javafx_sdk/lib/javafx.controls.jar" ]; then
        echo "JavaFX SDK not recognized. Exiting"
        exit 1
      fi
    else
      echo "Autodetected JavaFX SDK located at $javafx_sdk"
    fi
  elif [ "$option" = "2" ]; then
    prompt "JavaFX SDK install location" "Where should the JavaFX SDK be installed?" "$HOME/lib/javafx-sdk"
    javafx_sdk="$answer"
    mkdir -p "$javafx_sdk"
    wget https://gluonhq.com/download/javafx-11-0-2-sdk-linux/ -O /tmp/javafx.zip
    7z x /tmp/javafx.zip "-o$javafx_sdk"
    rm /tmp/javafx.zip
    mv "$javafx_sdk/javafx-sdk-11.0.2/lib" "$javafx_sdk"
    rm -r "$javafx_sdk/javafx-sdk-11.0.2"
  else
    echo "Invalid option. Exiting"
    exit 1
  fi
}

query_version() {
  prompt "Version" "Which version Hextant should be installed?" "Latest"
  version="$answer"
  if [ "$version" = "Latest" ]; then
    version="1.0-SNAPSHOT"
  fi
}

query_home() {
  prompt "Location to install Hextant" "Where should Hextant be installed?" "$HOME/hextant"
  hextant_home="$answer"
  echo "export HEXTANT_HOME=$hextant_home" >> ~/.bashrc
}

get_components() {
  mkdir -p "$hextant_home/plugins"
  get_jar "hextant-core-fatjar" "core.jar"
  get_jar "hextant-main" "main.jar"
  get_jar "hextant-launcher" "launcher.jar"
}

create_launcher() {
  mkdir -p "$hextant_home/launcher"
  wget https://raw.githubusercontent.com/NKb03/Hextant/master/setup/launcher-info.json -O "$hextant_home/launcher/project.json"
}

create_script() {
  command="#!/bin/sh
java --module-path $javafx_sdk/lib\
  --add-modules javafx.controls\
  --add-opens java.base/jdk.internal.loader=ALL-UNNAMED\
  -jar $hextant_home/plugins/main.jar \"\$@\"
"
  echo "$command" >"$hextant_home/hextant"
  chmod +x "$hextant_home/hextant"
}

create_alias() {
  echo "Create alias hextant startup script? (1) Yes, (2) No"
  read -r option
  if [ "$option" = "1" ]; then
    echo "alias hextant=$hextant_home/hextant" >> ~/.bashrc
  fi
}

install() {
  echo "" >> ~/.bashrc
  echo "" >> ~/.bashrc
  check_java
  setup_javafx_jdk
  query_version
  query_home
  get_components
  create_launcher
  create_script
  create_alias
}

update() {
  hextant_home="$HEXTANT_HOME"
  query_version
  get_components
}

echo "Welcome to the Hextant setup assistant!"
if [ -z "$1" ]; then
  echo "Usage: $0 {install|update}"
  exit 1
fi
case "$1" in
  install)
    install;;
  update)
    update;;
  *)
    echo "Unknown command $1. Valid commands 'install' and 'update'."
    exit 1
    ;;
esac
. ~/.bashrc
