#!/user/bin/env bash

# 쉬고 있는 profile 찾기: prod1이 사용 중이면 prod2가 쉬고 있고, 반대면 prod1이 쉬고 있음

function find_idle_profile()
{
  # -s: silent; -o /dev/null: 응답 데이터 리다이렉트 후 버림; -w http_code: 응답헤더에서 http_code만 추출함;
  RESPONSE_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost/profile)

  if [ ${RESPONSE_CODE} -ge 400 ] # ge: greater than or equal to
  then
    CURRENT_PROFILE=prod2
  else
    CURRENT_PROFILE=$(curl -s http://localhost/profile)
  fi

  if [ ${CURRENT_PROFILE} == prod1 ]
  then
    IDLE_PROFILE=prod2
  else
    IDLE_PROFILE=prod1
  fi

  echo "${IDLE_PROFILE}"
}

# 쉬고 있는 profile의 포트 찾기
function find_idle_port()
{
  IDLE_PROFILE=$(find_idle_profile)

  if [ ${IDLE_PROFILE} == prod1 ]
  then
    echo "8081"
  else
    echo "8082"
  fi
}