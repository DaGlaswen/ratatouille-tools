---
name: smart-ring-health-data
description: Help clients retrieve and analyze their health data from Smart Ring devices. Use when a user wants to check their heart rate, sleep quality, steps, stress levels, blood oxygen (SpO2), body temperature, or HRV (heart rate variability).
---

## Available Smart Ring MCP Tools

1. **getHeartRate** — get heart rate measurements in beats per minute
2. **getSleep** — get sleep session information with quality metrics
3. **getSteps** — get daily step count and activity metrics
4. **getStress** — get stress level measurements
5. **getSpO2** — get blood oxygen saturation measurements
6. **getHrv** — get heart rate variability measurements
7. **getTemperature** — get body temperature measurements
8. **getAllHealthStatistics** — get all health statistics in a single parallel request (heart rate, sleep, steps, stress, SpO2, HRV, temperature)

## Workflow

### Step 1: Identify the Requested Data Type
Determine which health metric the client wants to retrieve:
- Heart rate → use `getSyncHeartRate`
- Sleep → use `getSyncSleep`
- Steps → use `getSyncStep`
- Stress → use `getSyncStress`
- SpO2 → use `getSyncSpo2`
- HRV → use `getSyncHrv`
- Temperature → use `getSyncTemperature`

### Step 2: Determine Time Range (Optional)
If the client specifies a time range:
- Use ISO 8601 format for dates with **Moscow timezone (+03:00)**: `yyyy-MM-dd'T'HH:mm:ss +03:00`
- Use `from` parameter for the start date
- Use `to` parameter for the end date

**Important**: All dates must be in Moscow time (UTC+3). Convert client's local time to Moscow time before sending the request.

If no time range is specified, call the tool without date parameters to get the most recent data.

### Step 3: Call the Appropriate Tool
Call the selected tool with the appropriate parameters:
- `from` (optional): Start date in ISO 8601 format with Moscow time (`yyyy-MM-dd'T'HH:mm:ss +03:00`)
- `to` (optional): End date in ISO 8601 format with Moscow time (`yyyy-MM-dd'T'HH:mm:ss +03:00`)
- `page` (optional, default 0): Page number for pagination
- `pageSize` (optional, default 100): Number of records per page

### Step 4: Process and Present the Data
Interpret the response and present it in a user-friendly format:

**For Heart Rate:**
```
Your heart rate measurements:
- [Date/Time]: [heartRate] bpm
```

**For Sleep:**
```
Your sleep data:
- [Date]: [totalSleepTime] minutes, Quality: [sleepQuality], Score: [sleepScore]/100
  Average HRV: [avgHrv] ms, Min Heart Rate: [minHeartRate] bpm, Avg SpO2: [avgSpo2]%
```

**For Steps:**
```
Your step count:
- [Date]: [step] steps, Distance: [distance] km, Calories: [calories]
```

**For Stress:**
```
Your stress levels:
- [Date/Time]: [stress] (stress level)
```

**For SpO2:**
```
Your blood oxygen levels:
- [Date/Time]: [spo2]%
```

**For HRV:**
```
Your HRV measurements:
- [Date/Time]: [hrv] ms
```

**For Temperature:**
```
Your temperature measurements:
- [Date/Time]: NTC: [ntc]°, BOSH: [bosh]°
```

### Step 5: Handle Pagination
If the response indicates multiple pages (`currentPage` < `totalPages`):
- Inform the client that there are more records available
- Offer to retrieve additional pages if needed

## Available MCP Tools

1. **getSyncHeartRate** — Get heart rate (pulse) data from Smart Ring
2. **getSyncSleep** — Get sleep data from Smart Ring
3. **getSyncStep** — Get step count and activity data from Smart Ring
4. **getSyncStress** — Get stress level data from Smart Ring
5. **getSyncSpo2** — Get blood oxygen (SpO2) data from Smart Ring
6. **getSyncHrv** — Get heart rate variability (HRV) data from Smart Ring
7. **getSyncTemperature** — Get body temperature data from Smart Ring
8. **getAllHealthStatistics** — Get all health statistics in a single parallel request

## Tool Parameters

All tools accept the following optional parameters:
- `uuid`: Authorization UUID in Smart Ring system (if not provided, uses default from properties)
- `from`: Start date in ISO 8601 format with **Moscow time** (`yyyy-MM-dd'T'HH:mm:ss +03:00`) — e.g., `2025-03-25T00:00:00 +03:00`
- `to`: End date in ISO 8601 format with **Moscow time** (`yyyy-MM-dd'T'HH:mm:ss +03:00`) — e.g., `2025-03-26T23:59:59 +03:00`
- `page`: Page number (default: 0) — for individual endpoints only
- `pageSize`: Records per page (default: 100) — for individual endpoints only
- `limit`: Maximum records per data type (default: 100) — for `getAllHealthStatistics` only

**Note**: 
- `getAllHealthStatistics` returns `hasMoreData.XXX = true` if data is truncated — use individual endpoints to retrieve complete data.

## Important Rules

1. **Moscow Time (UTC+3)**: The client is in Moscow time zone. All date parameters MUST use Moscow time with +03:00 timezone offset (e.g., `2025-03-25T10:30:00 +03:00`)
2. **ISO 8601 format**: Use the format `yyyy-MM-dd'T'HH:mm:ss +03:00` for all date parameters
3. **Time Conversion**: If the client provides a time in their local timezone, convert it to Moscow time (UTC+3) before making the API call
4. **Pagination**: Default page size is 100 records; use pagination for large datasets
5. **Data interpretation**: Present data in a clear, user-friendly format
6. **Privacy**: Only retrieve data for the authenticated user
7. **Error handling**: Provide clear error messages if the API returns an error

## Common Use Cases

### Getting All Health Statistics at Once
```
Client: "Show me all my health data for yesterday"
Agent: [Calls getAllHealthStatistics with from="2025-03-24T00:00:00 +03:00", to="2025-03-24T23:59:59 +03:00"]

Response example:
{
  "heartRate": { "content": [...100 records...], "totalElements": 288, "isLast": false },
  "sleep": { "content": [...1 record...], "totalElements": 1, "isLast": true },
  "hasMoreData": {
    "heartRate": true,
    "sleep": false,
    "steps": false,
    "stress": false,
    "spo2": false,
    "hrv": false,
    "temperature": false,
    "any": true
  }
}

Agent: "Here's your health summary for March 24:
- Heart Rate: 100 of 288 measurements ⚠️ (showing first 8 hours)
- Sleep: 1 session (complete)
- Steps: 1 record (complete)
- Stress: 45 measurements (complete)
- SpO2: 100 of 150 measurements ⚠️
- HRV: 80 measurements (complete)
- Temperature: 100 of 120 measurements ⚠️

⚠️ Some data is truncated. Would you like to see the complete heart rate data for the full day?"
```

### Checking Today's Steps
```
Client: "How many steps did I take today?"
Agent: [Calls getSyncStep with from="2025-03-25T00:00:00 +03:00" and to="2025-03-25T23:59:59 +03:00" (Moscow time)]
"Today you took [step] steps, covering [distance] km and burning [calories] calories."
```

### Reviewing Sleep Quality
```
Client: "How was my sleep last night?"
Agent: [Calls getSyncSleep with last night's date range]
"Last night you slept for [totalSleepTime] minutes with a quality rating of [sleepQuality].
Your sleep score was [sleepScore]/100."
```

### Monitoring Heart Rate
```
Client: "Show me my heart rate data"
Agent: [Calls getSyncHeartRate without date parameters for recent data]
"Here are your recent heart rate measurements:
- [Date/Time 1]: [heartRate] bpm
- [Date/Time 2]: [heartRate] bpm"
```

### Checking Stress Levels
```
Client: "What's my stress level?"
Agent: [Calls getSyncStress for recent data]
"Your recent stress level measurement is [stress]."
```

### Blood Oxygen Monitoring
```
Client: "Check my blood oxygen levels"
Agent: [Calls getSyncSpo2 for recent data]
"Your recent SpO2 measurement: [spo2]%"
```

### HRV Analysis
```
Client: "Show me my HRV data"
Agent: [Calls getSyncHrv for recent data]
"Your recent HRV measurements:
- [Date/Time]: [hrv] ms"
```

### Temperature Tracking
```
Client: "What's my body temperature?"
Agent: [Calls getSyncTemperature for recent data]
"Your recent temperature measurements:
- [Date/Time]: NTC: [ntc]°, BOSH: [bosh]°"
```

## Error Handling

- **400 Bad Request**: Inform the client that the request parameters are invalid
- **401 Unauthorized**: Authentication error — token is missing or invalid
- **403 Forbidden**: Access denied to the requested data
- **429 Rate Limit**: Too many requests — wait before retrying
- **500 Internal Server Error**: Smart Ring API server error
- **503 Service Unavailable**: Smart Ring service is temporarily unavailable
- **Timeout**: Connection timeout — inform the client and retry

## Example Dialogue

```
Client: "I want to check my sleep data from last week"
Agent: [Calls getSyncSleep with appropriate date range]
"Here's your sleep data from last week:

- Monday: 420 minutes, Quality: Good, Score: 85/100
- Tuesday: 390 minutes, Quality: Fair, Score: 72/100
- Wednesday: 450 minutes, Quality: Excellent, Score: 92/100

Would you like to see more details or check other health metrics?"

Client: "Show me my steps for today"
Agent: [Calls getSyncStep with today's date]
"Today you've taken 8,542 steps, covering 6.2 km and burning 420 calories."

Client: "What's my heart rate?"
Agent: [Calls getSyncHeartRate for recent data]
"Your recent heart rate measurements:
- 10:30 AM: 72 bpm
- 11:00 AM: 75 bpm
- 11:30 AM: 68 bpm"
```

## Health Insights

When presenting data, you can provide basic health insights:

- **Heart Rate**: Normal resting heart rate is 60-100 bpm
- **Sleep**: Adults typically need 7-9 hours (420-540 minutes) of sleep
- **Steps**: 10,000 steps per day is a common fitness goal
- **SpO2**: Normal blood oxygen levels are 95-100%
- **HRV**: Higher HRV generally indicates better recovery and lower stress
- **Stress**: Lower stress scores indicate better stress management
