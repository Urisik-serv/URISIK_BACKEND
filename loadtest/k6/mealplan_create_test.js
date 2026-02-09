import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 1,          // 동시 사용자 수 (hey -c 1 대응)
    iterations: 50,  // 총 요청 수 (hey -n 50 대응)
    maxDuration: '30m',
    gracefulStop: '30s',
};

const BASE_URL = 'http://localhost:8080';
const FAMILY_ROOM_ID = 3;

const TOKEN = __ENV.K6_TOKEN;

if (!TOKEN) {
  throw new Error('Missing K6_TOKEN env var. Set: K6_TOKEN="Bearer <TOKEN>"');
}

export default function () {
  const url = `${BASE_URL}/api/family-rooms/${FAMILY_ROOM_ID}/meal-plans`;

  const payload = JSON.stringify({
    weekStartDate: "2026-12-12",
    selectedSlots: [
      { mealType: "LUNCH", dayOfWeek: "MONDAY" },
      { mealType: "DINNER", dayOfWeek: "MONDAY" },
      { mealType: "LUNCH", dayOfWeek: "TUESDAY" },
      { mealType: "DINNER", dayOfWeek: "TUESDAY" },
      { mealType: "LUNCH", dayOfWeek: "WEDNESDAY" },
      { mealType: "DINNER", dayOfWeek: "WEDNESDAY" },
      { mealType: "LUNCH", dayOfWeek: "THURSDAY" },
      { mealType: "DINNER", dayOfWeek: "THURSDAY" },
      { mealType: "LUNCH", dayOfWeek: "FRIDAY" },
      { mealType: "DINNER", dayOfWeek: "FRIDAY" },
      { mealType: "LUNCH", dayOfWeek: "SATURDAY" },
      { mealType: "DINNER", dayOfWeek: "SATURDAY" },
      { mealType: "LUNCH", dayOfWeek: "SUNDAY" },
      { mealType: "DINNER", dayOfWeek: "SUNDAY" }
    ],
    regenerate: true
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': TOKEN,
    },
    timeout: '60s',
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1);
}