# Eltex STA Collector


Сборка проекта jar + deb:

```
mvn clean install
mvn clean package
```

При установке deb пакета на хосте должны быть установлены:

* Java (>= 1.8.161)
* Cassandra (>= 3.11.2)

### Установка Cassandra

Наиболее простой способ установки cassandra - через apt репозиторий. Данный способ описан в официальной документации:
[тут](http://cassandra.apache.org/download/)

По умолчанию после установки cassandra начинает работать на интерфейсе localhost. Чтобы подключиться к cassandra, можно воспользоваться утилитой cqlsh:

```
~$ cqlsh
Connected to my_cluster at 127.0.0.1:9042.
[cqlsh 5.0.1 | Cassandra 3.11.2 | CQL spec 3.4.4 | Native protocol v4]
Use HELP for help.
cqlsh>
```

Если подключение удалось, то это означает, что cassandra работает. После этого можно устанавливать sta collector.

### Установка STA Collector

* Скопировать собранный deb-пакет на устанавливаемый хост
* Выполнить установку пакета с помощью утилиты dpkg

```
ems@ubuntu-sink:~$ sudo dpkg -i eltex-sta-collector-1.10-30_all.deb
[sudo] password for ems:
(Reading database ... 125824 files and directories currently installed.)
Preparing to unpack eltex-sta-collector-1.10-30_all.deb ...
Prepare to remove old version eltex-sta-collector before upgrade
Stopping eltex-sta-collector ...
eltex-sta-collector stopped
Prepare to upgrade packet eltex-sta-collector
Unpacking eltex-sta-collector (1.10-30) over (1.10-30) ...
Setting up eltex-sta-collector (1.10-30) ...
The system user `stacollector' already exists. Exiting.
eltex-sta-collector starting
......................Service eltex-sta-collector started
Processing triggers for ureadahead (0.100.0-16) ...
```

После установки сервис должен запуститься.


### Работа с сервисом

* Конфиг расположен в */etc/eltex-sta-collector/macs.properties*
* Логи раполагаются по пути */var/log/eltex-sta-collector/*
* По умолчанию сервис поднимается на порту 8090. Для изменения можно поменять параметр в конфиге _server.port_
* Сервис можно запустить в качестве приложения с выводом логов на экран:
```
java -jar /usr/share/eltex-sta-collector/eltex-sta-collector-exec.jar
```

### URLs

#### HTTP API
* _/addmac_ - добавление записи MAC в базу.

Параметры:
apmac - MAC точки доступа
stamac - MAC клиента
band - диапазон, принимает значения 2 - для 2.4G или 5 - для 5G (опц.)
rssi - уровень сигнала (опц.)
domain - домен точки (опц.)

Пример:
http://192.168.26.127:8090/addmac?apmac=00:11:22:33:44:55&stamac=03:11:33:44:55:66&band=5&rssi=-40

* _/getStat_ - получение статистики по всем записями в базе. Без параметров

При запросе выведет что-то типа такого:
```
--- STAT ---
 Total Entries in table: 1001
 Total unic AP MAC: 253
 Total unic STA MAC: 253
    2.4G: 524; Avg RSSI: -47.0
    5G: 477; Avg RSSI: -44.0
```
* _/getApUsage_ - вадает json со списком MAC клиентов у каждой точки, либо только для точки указанной в параметре _apmac_ (опц.)

Пример:
http://192.168.26.127:8090/getApUsage?apmac=00:11:22:33:44:55

* _/getStaUsage_ - вадает json со списком MAC точек по каждому клиентскому устройству, либо для устройства указанного в параметре _stamac_ (опц.)

Пример:
http://192.168.26.127:8090/getStaUsage?stamac=00:11:22:33:44:55


#### WEBSOCKET API
* _/add_mac_ - добалвение записи в базу. Принимает JSON в качестве данных для добавления в базу.
Формат принимаемого JSON:

```
{"apDomain": "test_domain", "rssi": "-31", "apMac": "A8:B7:11:00:00:8E", "band": "2", "staMac": "00:01:11:00:00:5C"}
```

### Исползованные библиотеки/фреймворки/тулзы

* Spring Boot 1.5.10 (Spring 4.3.11)
Пробовалось использовать Spring Boot 2.0 c Spring 5.0 на борту, но он оказался не совместим со Spark ниже 2.2.1 (c 2.3.0 работает), по причине конфликтности версии Netty (Spark 2.2.x использует Netty более старой версии 4.0.x, Spring 5 - Netty 4.1.x) Почему нельзя использовать Spark 2.3.0 - описано ниже.
* Spring Data (льет данные в cassandra)
* Cassandra 3.11.2
Более старшие вресии Cassandra не будут совместимы с новой версии JVM. А новая версия Cassandra не будет совместима с более старыми версиями JVM (старше чем 1.8.161) - https://issues.apache.org/jira/browse/CASSANDRA-14173. По этому тут лучше убедиться что всё свежее!
* Spark 2.2.1
Пробовалось использовать Spark 2.3.0, но у данной версии сломан cassandra-connector https://datastax-oss.atlassian.net/browse/SPARKC-530. Без этого теряется смысл использования Spark. По этому используем чуть более старую, но работающую с Cassandra.



