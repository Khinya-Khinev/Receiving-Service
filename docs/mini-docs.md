# Основные понятия 
- SKU stock keeping unit - айди товара
- LPN licence plate number - айди тары
- ASN advanced shipping notice - список того что приедет

Вообще очень много видов приемок и очень много нюянасов. Я придумал что-то простенькое,
чтобы было где найти проблем и потыкать свой стек - поэтому где-то упрощено, где-то странно, где-то не хватает фичей.
От скалада к складу все может отлиаться - я взял 1 из сценариев: 
- ASN_MATCHING - сверка с доком поставки (пока не реализовано)
- сканирование каждой палеты + сканирование sku и ручной ввод количества

---

# Сущности
- AdvancedShippingNotice (поставка) - содержимое поставки и др
- GoodsReceipt(приемка) - квитанция самой приемки или другими словами факт прибытия
- к факту прибытия подвязаны WorkerReceivingSession (сессии приемки работников)

---

# Роли
- менеджер создает поставку(это вообще отдельный сервис по идее должен делать но пофик), 
открывает и закрывает приемку, принимать товар тоже может
- раб тока принимает

---

# Весь процесс от начала до конца 
1. создается заранее документик с поставкой - что привезут на склад короче
2. машина приезжает - открывается менеджером GoodsReceipt - факт приемки - на время сканирования товара
3. любой желающий раб на складе выбирает открытую приемку и начинает в рамках ее принимать товар 
4. сканирует коробку - она становиться текущей
5. скаинурет еще коробку - она влаживается в предыдущую и станвоится текущей уже а прошлая запоминается и становится предыдущей
6. в любой момент можно добавить товар в текущую коробку
7. можно вернуться назад на уровень вложенности
8. (никаких фичей типо редактирования или отмены я не добавлял)
9. в конце раб завершает свою сессию приемки работника - система раба освобождает
10. менеджер закрывает приемку
11. закрывается поставка как принятая 
12. формиурется отчет по расхождениям (тоже можно добавить ui для этого, но пока и так хватает наверн)

---

# Эндпоинты

### Advanced Shipping Notice
| Method | Endpoint                                  | Description                        |
|--------|--------------------------------------------|-------------------------------------|
| GET    | `/api/asns/search`                          | Search ASNs by filter criteria      |
| GET    | `/api/asns/{asn_id}`                        | Get ASN details by ID               |
| GET    | `/api/asns/{asn_id}/handling-units`         | List handling units for an ASN      |
| POST   | `/api/asns`                                 | Create a new ASN                    |

### Auth
| Method | Endpoint                | Description                         |
|--------|--------------------------|--------------------------------------|
| POST   | `/api/auth/managers`     | Register a manager account          |
| POST   | `/api/auth/workers`      | Register a worker account           |
| POST   | `/api/auth/login`        | Authenticate and obtain JWT tokens  |
| POST   | `/api/auth/refresh`      | Refresh access token                |

### Goods Receipt
| Method | Endpoint                                        | Description                          |
|--------|--------------------------------------------------|----------------------------------------|
| GET    | `/api/goods-receipts`                             | List goods receipts                   |
| GET    | `/api/goods-receipts/{receipt-id}/received-units` | List received units for a receipt     |
| POST   | `/api/goods-receipts`                             | Create a new goods receipt            |
| POST   | `/api/goods-receipts/{receipt-id}/closure`        | Close out a goods receipt             |

### Receiving Process
| Method | Endpoint                                                  | Description                                |
|--------|-------------------------------------------------------------|-----------------------------------------------|
| GET    | `/api/receiving-sessions/validations/lpn/{lpn}`              | Validate a license plate number (LPN)         |
| GET    | `/api/receiving-sessions/validations/sku/{sku}`               | Validate a SKU                                |
| POST   | `/api/receiving-sessions/{receiptId}/joins`                   | Join a worker to a receiving session          |
| POST   | `/api/receiving-sessions/scans/{lpn}`                          | Scan a handling unit by LPN                   |
| POST   | `/api/receiving-sessions/scans/contents/{sku}`                 | Scan contents of a handling unit by SKU       |
| POST   | `/api/receiving-sessions/navigation/back`                      | Navigate back to the previous session step    |
| POST   | `/api/receiving-sessions/completion`                            | Complete a receiving session                  |


# Идмепотентность 
Нужно генерировать на стороне клиента айди запроса и передавать в заголовок X-Idempotency-Key

src/main/java/com/waregang/receiving_service/
├── advanced_shipping_notice/       ### ASN domain (ASNs, expected handling units, contents, arrival timelines)
│   ├── api/                        # REST Controllers & DTOs for managing ASNs
│   ├── application/                # Application services & mapping logic
│   ├── domain/                     # Domain models (AdvancedShippingNotice, HandlingUnit, Content)
│   └── infrastructure/             # Database adapters & JPA repositories
├── receiving_process/              ### Core goods receiving execution engine
│   ├── api/                        # Scanning endpoints, receiving session controllers & DTOs
│   ├── application/                # Receiving process and Goods Receipt services
│   │   └── ports/                  # Ports used by application service
│   ├── domain/                     # Domain models (GoodsReceipt, WorkerReceivingSession, ReceivedUnit)
│   └── infrastructure/             # Persistence adapters & JPA repositories
├── integration/                    ### Event-driven external system integrations
│   ├── discrepancies_report/       # Kafka adapter and service for discrepancy reports
│   └── putaway/                    # Kafka adapter and service for putaway notifying
├── security/                       ### Security configuration, JWT authentication, user management
└── common/                         ### Cross-cutting concerns (idempotency interceptors, global exception handling, custom validation annotations, app-side UUID generation)
```