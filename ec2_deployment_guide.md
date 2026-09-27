# Web 打卡系統 - AWS EC2 部署與維護手冊

本手冊彙整了從零開始設定 AWS EC2 伺服器，到結合 GitHub Actions 完成 CI/CD 自動化部署的完整操作流程，並包含日後伺服器開關機的重要維護須知。

---

## 🚀 一、首次建立與部署流程

### 1. AWS EC2 基礎設定
1. 在 AWS 啟動一台 **Amazon Linux** 執行個體 (Instance)。
2. 建立並下載好 `.pem` 登入私鑰，請妥善保管。
3. **設定防火牆 (Security Group)**：
   * **Port 22 (SSH)**：為了讓 GitHub Actions 部署與您個人遠端操作，設定為 `0.0.0.0/0` (Anywhere IPv4)。
   * **Port 80 (HTTP)**：這是對外提供網頁服務的通道，請設定為 `0.0.0.0/0` (若在測試階段想防護，可設定為 `My IP`)。

### 2. EC2 環境初始化 (安裝 Docker)
使用終端機與 `.pem` 鑰匙 SSH 進入 EC2 後，依序貼上以下指令來打造地基：

```bash
# 更新系統與安裝基本套件
sudo yum update -y
sudo yum install git docker -y

# 啟動 Docker 服務並設定開機自啟
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# 安裝最新版 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 安裝 Buildx 擴充套件 (Docker Compose 構建需要)
sudo mkdir -p /usr/local/lib/docker/cli-plugins
sudo curl -L "https://github.com/docker/buildx/releases/download/v0.17.1/buildx-v0.17.1.linux-amd64" -o /usr/local/lib/docker/cli-plugins/docker-buildx
sudo chmod +x /usr/local/lib/docker/cli-plugins/docker-buildx

# 將專案 Clone 下來 (替換成您的專案網址)
git clone https://github.com/您的帳號/punch_side_project.git
```

### 3. 設定 GitHub Actions (CI/CD)
前往您的 GitHub 專案頁面 -> **Settings** -> **Secrets and variables** -> **Actions**，新增以下三個 Repository secrets：
* **`EC2_HOST`**：您的 EC2 Public IP (例如 `123.45.67.89`)
* **`EC2_USERNAME`**：`ec2-user`
* **`EC2_SSH_KEY`**：貼上您 `.pem` 檔案內的全部文字 (包含 BEGIN 與 END 那兩行)

設定完成後，只要在本地端將程式碼 Commit 並 Push 到 `main` 分支，GitHub 就會自動登入 EC2 執行更新與部署！

---

## 🛠️ 二、日常維護與開關機注意事項

### 1. 安全關機 (Stop Instance)
不需要在終端機敲任何指令，您可以直接在 AWS 主控台對該台 EC2 點選 **「Stop instance」**。Linux 作業系統會自動且安全地向 Docker 傳達關機訊號，資料庫與系統皆會安全停止，不會造成資料損壞。

### 2. 重開機後的注意事項 (⚠️ 極度重要)
當您將 EC2 重新啟動 (Start instance) 後，請務必依序執行以下三件事，系統才能恢復正常運作：

#### 📝 A. 確認並更新 Public IP
AWS EC2 只要經歷過關機再開機，**預設的 Public IPv4 位址就會重新分配（換一個新的 IP）**。
* **做法**：開機後，請到 AWS 主控台複製這台機器獲得的「新 IP」。

#### 📝 B. 更新 GitHub Secrets
由於 IP 已經改變，GitHub Actions 會找不到舊的伺服器。
* **做法**：前往 GitHub 的 Secrets 設定頁面，將 **`EC2_HOST`** 的值更新為您剛剛複製的「新 IP」。

#### 📝 C. 手動喚醒容器服務
因為我們的架構沒有設定容器無限自動重啟，所以開機後網頁是不會通的。
* **做法**：使用 SSH 連入 EC2，並手動輸入以下指令將服務喚醒：
  ```bash
  cd /home/ec2-user/punch_side_project
  sudo /usr/local/bin/docker-compose up -d
  ```
  *(或者您也可以在本地端隨便推一個 git commit 來觸發 GitHub Actions 幫您重新部署喚醒)*

> **💡 進階建議 (如何避免一直換 IP？)**
> 如果您覺得每次重開機都要更新 IP 太麻煩，您可以在 AWS 申請一個 **「Elastic IP (彈性 IP)」** 並綁定到這台 EC2 上。如此一來，不管怎麼關機重開，您的對外 IP 永遠都不會改變了！
