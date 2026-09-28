# WEB打卡系統-系統分析暨設計書
**專案名稱**: Web punch system
**版本**: v1.0
**目標系統**: Vue3, Spring boot 3, postgreSQL(包含API Payload 加密)

# 一、系統分析規格
## 背景與目標
為提供便捷、防竄改且安全的出勤打卡管道，故建構一套 Web 端打卡工具。
* **主要目的**：支援使用者每日「上班」、「下班」打卡的功能，並可以即時查詢打卡紀錄
* **資料安全傳輸**：為確保資料傳輸時是安全不被串改，所以api req body需要加密處理

## 使用者角色與權限
| 角色名稱 | 角色代碼 | 權限範圍 |
| :--- | :--- | :--- |
| 員工 | `EMPLOYEE` | 1. 檢視打卡介面與伺服器時間。<br>2. 提交上班/下班打卡（加密發送）。<br>3. 檢視歷史打卡清單。 |

## 功能需求清單
* **FR-1 系統即時時間顯示**：前端頁面需要顯示即時的系統時間，且以秒為單位進行更新
* **FR-2 使用者身份辨識**：提供員編輸入欄位，且要驗證不可空白
* **FR-3 動作辨識**：提供上班/下班/查詢紀錄的功能按鈕
* **FR-4 資料傳輸加密**：前端提出需求至後端前需要將資料封裝成JSON格式並且轉換成base64＋加密處理
* **FR-5 資料解密與處理**：後端接收密文後解密為原始資訊後轉換物件並加上時間戳記寫入DB
* **FR-6 歷史打卡紀錄**：預設查詢近一週資料，查詢時使用打卡時間戳記倒序排序，顯示員編、時間、打卡別

## 業務邏輯與流程
### 打卡正常流程
1. 使用者載入頁面
2. 使用者在輸入欄輸入自己所屬的員工編號
3. 使用者點擊「上班」、「下班」按鈕
4. 前端驗證資料合理性
    - 如果空白就不接續向後端發出請求，另外於頁面顯示錯誤訊息
5. 前端將資料轉換JSON格式，再轉換成base64格式後加密
6. 前端將加密完成資訊包裝成api req發送至後端
7. 後端接收到api req
    7.1. 將req body解密還原成JSON格式
    7.2. 將JSON轉換為物件
    7.3. 寫入DB（存入當下需要帶入系統時間）
    7.4. 回傳成功以及打卡相關資訊給前端
8. 前端接收到200 rsp後顯示彈窗展示打卡資訊與成功字樣

### 打卡例外流程
* **情境A-欄位空白**：使用者沒有輸入資訊直接打卡 -> 終止後續步驟不發送API
* **情境B-解密失敗**：後端解密出現失敗 -> 回傳500狀態碼，前端顯示「打卡需求產生例外狀況，請通知系統管理員；錯誤碼: 801」
* **情境C-未知的員編**：後端驗證員編發現無此員編 -> 回傳400狀態碼，前端顯示「未知的員編；錯誤碼: 001」-> 清空輸入，回到原始的頁面
* **情境D-非法的員編**：後端驗證員編發現格式錯誤 -> 回傳400狀態碼，前端顯示「錯誤的員編格式，請重新操作一次；錯誤碼: 002」-> 清空輸入，回到原始的頁面
* **情境E-DB錯誤**：後端JPA出現例外 -> 回傳500狀態碼，前端顯示「打卡需求產生例外狀況，請通知系統管理員；錯誤碼: 701」-> 清空輸入，回到原始的頁面
* **情境F-api timeout**：前端未在設定時間內取得api rsp -> 前端顯示「與後台溝通產生例外狀況，請通知系統管理員；錯誤碼: 601」-> 清空輸入，回到原始的頁面
* **情境G-查詢無打卡資料**：後端接收查詢需求發現無資訊 -> 回傳400狀態碼，前端顯示「目前查無打卡資料；錯誤碼: 003」-> 清空輸入，回到原始的頁面
* **情境H-查詢範圍異常**：後端驗證查詢日期範圍發現異常 -> 回傳400狀態碼，前端顯示「查詢範圍錯誤，請重新輸入一次；錯誤碼: 004」-> 維持目前頁面與輸入內容
* **情境I-查詢範圍過大**：後端驗證查詢日期範圍超過1個月 -> 回傳400狀態碼，前端顯示「查詢範圍超過1個月，請重新輸入一次，若需要超過一個月查詢需求，請洽系統管理員；錯誤碼: 005」-> 維持目前頁面與輸入內容
* **情境J-其他未知錯誤**：後端出現非上述的例外狀況 -> 回傳500狀態碼，前端顯示「打卡需求產生例外狀況，請通知系統管理員；錯誤碼: 901」

## 非功能需求
* **安全性**
- api body使用加密保護
- 使用伺服器時間防止前端竄改打卡資料

* **可用性**
- 前端頁面使用RWD來適應各裝置
- 打卡按鈕在請求過程中全部需轉換成disable，避免發出多次請求

* **日誌紀錄 (Logging)**
- **後端 (AP Log)**：採用 Log4j2 作為日誌框架。
  - **Console 輸出**：方便開發階段進行即時錯誤追蹤。
  - **Rolling File (檔案歸檔)**：正式環境日誌將輸出至 `logs/punch-backend.log`。系統每天會自動將舊日誌壓縮歸檔（例如：`punch-backend-YYYY-MM-DD.log.gz`），並設定最高保留 30 天，避免硬碟空間耗盡。
  - **日誌層級**：預設為 INFO，針對特定業務邏輯（如打卡失敗、解密錯誤等）將使用 ERROR 或 WARN 層級以利排錯。
- **前端 (Web Log)**：目前暫不導入第三方日誌收集平台或層級日誌套件，維持瀏覽器預設的開發者工具除錯。

# 二、系統設計規格
## 系統架構設計
### 系統層級架構
* **前端**: Vue3 + bootstrap + [加密]
* **需求接收接收層**: string boot 3，負責處理 HTTP 協定與解密資料流
* **業務邏輯層**: 資料檢核、Jackson JSON 反序列化與物件轉換
* **持久層**: Spring Data JPA
* **資料層**: PostgreSQL 
* **日誌追蹤層**: Log4j2 (取代預設的 Logback)

### 加解密機制規格
* **加密演算法 (Cipher)**：RSA (非對稱加密)
* **運作模式 (Mode)**：ECB
* **填充方式 (Padding)**：OAEPWithSHA-256AndMGF1Padding
* **金鑰長度**：2048-bit
* **編碼方式**：明文以 `UTF-8` 編碼，密文以 `Base64` 字串封裝傳輸。
* **金鑰管理**：前端配置公鑰(Public Key)進行加密，後端配置私鑰(Private Key)進行解密。

## 資料庫設計
### 資料表規格
#### `employee`
員工資料表
| 欄位名稱 (Column) | 資料型態 (Type) | 鍵值 (Key) | 允許空值 (Null) | 說明與備註 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | PK (Auto Increment) | NO | 主鍵，自動遞增流水號 |
| `employee_id` | `VARCHAR(8)` | - | NO | 員工編號 (例: 00000000) |
| `employee_name` | `VARCHAR(16)` | - | NO | 打卡類別 (例: 陳大明) |

#### `punch_record`
用於紀錄每次的打卡需求
| 欄位名稱 (Column) | 資料型態 (Type) | 鍵值 (Key) | 允許空值 (Null) | 說明與備註 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `BIGINT` | PK (Auto Increment) | NO | 主鍵，自動遞增流水號 |
| `employee_id` | `VARCHAR(8)` | - | NO | 員工編號 (例: 00000000) |
| `punch_type` | `BIGINT` | - | NO | 打卡類別 (例: 1-上班, 0-下班) |
| `punch_time` | `TIMESTAMP` | Index | NO | 打卡時間戳記 (伺服器時間) |

### DDL語法

## api 介面規格
### 提交打卡紀錄
* **Method**: POST
* **Path**: /api/punches
* **Content-Type**: application/json

#### Request body
**實際傳輸密文**
```JSON
{
  "data": "k3gE9+4xTylH2K/..."
}
```

**解密後對應格式**
```JSON
{
  "employeeId": "EMP-001",
  "punchType": "上班"
}
```

#### Response body
**status code 200**
```JSON
{
  "id": 1,
  "employeeId": "00000000",
  "punchType": "1",
  "punchTime": "2026-09-26T10:30:00"
}
```

**status code 400**
```Plaintext
未知的員編；錯誤碼: ###
```

**status code 500**
```Plaintext
打卡需求產生例外狀況，請通知系統管理員；錯誤碼: ###
```

### 查詢打卡紀錄
* **Method**: GET
* **Path**: /api/punches

#### Response body
**status code 200**
```JSON
[
  {
    "id": 2,
    "employeeId": "00000000",
    "punchType": "0",
    "punchTime": "2026-09-26T18:00:00"
  },
  {
    "id": 1,
    "employeeId": "00000000",
    "punchType": "1",
    "punchTime": "2026-09-26T09:00:00"
  }
]
```

**status code 400**
```Plaintext
目前查無打卡資料；錯誤碼: ###
```

## 錯誤碼定義表
為方便前後端開發與維護，統整系統中定義之自訂錯誤碼、對應之 HTTP 狀態碼與情境描述：

| 錯誤碼 (Code) | HTTP 狀態碼 | 錯誤分類 | 錯誤訊息 (前端顯示提示) | 觸發情境說明 |
| :--- | :--- | :--- | :--- | :--- |
| `001` | 400 | 業務邏輯錯誤 | 未知的員編 | 後端驗證發現無此員工編號 |
| `002` | 400 | 輸入驗證錯誤 | 錯誤的員編格式，請重新操作一次 | 後端驗證員工編號格式不符 |
| `003` | 400 | 業務邏輯錯誤 | 目前查無打卡資料 | 查詢期間內沒有任何打卡紀錄 |
| `004` | 400 | 輸入驗證錯誤 | 查詢範圍錯誤，請重新輸入一次 | 查詢日期起訖範圍不合理 |
| `005` | 400 | 輸入驗證錯誤 | 查詢範圍超過1個月，請重新輸入一次，若需要超過一個月查詢需求，請洽系統管理員 | 限制單次查詢的最大時間跨度 |
| `601` | N/A | 網路與連線錯誤 | 與後台溝通產生例外狀況，請通知系統管理員 | 前端未在設定時間內取得 API Response (Timeout) |
| `701` | 500 | 資料庫錯誤 | 打卡需求產生例外狀況，請通知系統管理員 | 後端 JPA/DB 操作發生例外錯誤 |
| `801` | 500 | 安全與加密錯誤 | 打卡需求產生例外狀況，請通知系統管理員 | 後端解密 Payload 時發生失敗 |
| `901` | 500 | 系統未預期錯誤 | 打卡需求產生例外狀況，請通知系統管理員 | 其他未被捕捉的後端例外錯誤 |

## 模組與結構設計
### 後端
* com.example.punch.entity.PunchRecord：JPA 實體類別，對應資料表 punch_records
* com.example.punch.entity.Employee：JPA 實體類別，對應資料表 punch_records
* com.example.punch.dto.EncryptedRequest：承接前端密文 Payload
* com.example.punch.dto.PunchRequest：解密後的打卡請求資料傳輸物件
* com.example.punch.util.RsaCryptoUtil：提供靜態解密方法 decrypt(String encryptedBase64) 負責使用私鑰解密
* com.example.punch.repository.PunchRecordRepository：繼承 JpaRepository，宣告 findAllByOrderByPunchTimeDesc()
* com.example.punch.repository.EmployeeRepository：繼承 JpaRepository
* com.example.punch.controller.PunchService：資料庫存取與相關業務邏輯
* com.example.punch.controller.PunchController：負責協調解密、資料驗證與 API 回應

### 前端
* **狀態 (Refs)**：
- employeeId：雙向綁定輸入框。
- currentTime：定時更新之字串。
- records：儲存後端回傳的紀錄陣列。
- loading：防連點狀態旗標。
* **方法 (Methods)**：
- encryptData(plainObject)：呼叫 JSEncrypt 或 node-forge 執行 RSA 公鑰加密。
- handlePunch(punchType)：處理加密與 POST 呼叫。
- loadRecords()：呼叫 GET 取得清單。