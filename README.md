# Sistem Informasi Peminjaman Ruangan

Aplikasi web untuk mendukung proses peminjaman ruangan. Backend dibuat dengan Java dan Spring Boot, menggunakan MySQL sebagai database. Frontend statis (HTML, CSS, dan JavaScript) disertakan dalam aplikasi, sedangkan Nginx bertindak sebagai reverse proxy.

## Fitur

### Mahasiswa/pengguna

- Melihat dashboard mahasiswa.
- Menelusuri informasi ruangan dan melihat detail ruangan.
- Mengajukan peminjaman ruangan melalui halaman booking.
- Melakukan check-in untuk peminjaman.
- Melihat riwayat peminjaman.
- Melihat dan mengelola profil.

### Admin

- Melihat ringkasan pada dashboard admin.
- Mengelola data ruangan.
- Mengelola data mahasiswa.
- Mengelola booking/peminjaman.
- Melihat laporan.
- Melihat log pelanggaran.
- Mengakses halaman pengaturan.

### Sistem

- Autentikasi dan keamanan akses berbasis Spring Security dan JWT.
- Validasi data menggunakan Spring Validation.
- Penyimpanan data dengan MySQL dan Spring Data JPA.
- Bootstrap akun admin pertama melalui konfigurasi environment jika admin belum tersedia.
- Dokumentasi API melalui Springdoc OpenAPI.

> Rincian alur dan aturan setiap fitur mengikuti implementasi aplikasi yang tersedia di repository.

## Dokumentasi desain

### Use case diagram

![Use case diagram](docs/images/use-case.png)

> **TODO:** Tambahkan gambar use case ke `docs/images/use-case.png` agar tampil di README.

### Class diagram

![Class diagram](docs/images/class-diagram.png)

> **TODO:** Tambahkan gambar class diagram ke `docs/images/class-diagram.png` agar tampil di README.

### Desain Figma

[🔗 Buka desain di Figma](https://www.figma.com/) — **TODO:** ganti tautan ini dengan URL file/prototype Figma proyek.

## Teknologi

- Java 25 dan Spring Boot
- Maven
- MySQL 8.4
- Nginx
- Docker dan Docker Compose
- Spring Data JPA, Spring Security, JWT, dan Springdoc OpenAPI

## Menjalankan dengan Docker Compose

### Prasyarat

- Docker dengan Docker Compose
- OpenSSL untuk membuat pasangan kunci JWT

### 1. Siapkan konfigurasi lingkungan

Salin contoh konfigurasi, lalu ubah seluruh nilai contoh dengan kredensial yang kuat:

```bash
cp .env-example .env
```

Atur setidaknya `DB_PASSWORD`, `DB_ROOT_PASSWORD`, dan `ADMIN_PASSWORD` di file `.env`. Sesuaikan juga `DB_NAME`, `DB_USER`, `ADMIN_USERNAME`, `JAVA_VERSION`, atau `WEB_PORT` jika diperlukan. Jangan commit file `.env`.

### 2. Buat kunci JWT

Aplikasi membaca kunci dari folder `keys` pada host. Buat pasangan kunci RSA:

```bash
mkdir -p keys
openssl genpkey -algorithm RSA -out keys/private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl pkey -in keys/private_key.pem -pubout -out keys/public_key.pem
```

Jaga `private_key.pem` agar tetap rahasia. Folder kunci dipasang ke container secara read-only.

### 3. Bangun dan jalankan aplikasi

```bash
docker compose up --build -d
```

Aplikasi akan tersedia melalui Nginx di `http://localhost` secara default. Jika `WEB_PORT` diubah, gunakan port tersebut, misalnya `http://localhost:8081`.

Periksa status container dan log:

```bash
docker compose ps
docker compose logs -f app
```

Hentikan layanan:

```bash
docker compose down
```

Untuk menghapus data database yang tersimpan di volume Docker juga (tindakan ini menghapus data), jalankan:

```bash
docker compose down -v
```

## Database

MySQL menggunakan volume Docker `db_data` agar data tetap tersimpan saat container dihentikan. Skrip SQL di `db/init` dijalankan saat pertama kali volume database dibuat. Jika volume sudah terisi, skrip inisialisasi tidak dijalankan ulang.

## Pengembangan dan pengujian

Jalankan pengujian Maven dengan Maven Wrapper:

```bash
./mvnw test
```

Di Windows:

```bat
mvnw.cmd test
```

Untuk membangun paket aplikasi tanpa Docker:

```bash
./mvnw clean package
```

## Struktur penting

- `src/` — kode aplikasi serta resource frontend
- `db/init/` — skrip inisialisasi database
- `nginx/` — konfigurasi reverse proxy
- `Dockerfile` — proses build dan image aplikasi
- `docker-compose.yml` — konfigurasi layanan aplikasi, database, dan Nginx
- `.env-example` — contoh variabel lingkungan
- `docs/images/` — tempat menyimpan gambar dokumentasi seperti diagram use case dan class diagram

## Keamanan

- Gunakan password yang kuat dan jangan menyimpan `.env` atau private key di Git.
- Secara default, hanya Nginx yang mempublikasikan port ke host; database dan aplikasi berada di jaringan internal Docker.
- Untuk penggunaan produksi, gunakan HTTPS, simpan rahasia melalui pengelola secret yang sesuai, dan tinjau konfigurasi deployment sebelum dipublikasikan.

## Lisensi

Belum ada informasi lisensi yang ditentukan di repository ini.
