# 숫자 퀴즈 (Number Quiz)

## 소개
숫자에 대한 설명을 보고 이 숫자가 어떤 숫자인지 맞히는 간단한 Android용 숫자 게임입니다.

## API 키 설정
이 애플리케이션은 문제를 가져오기 위해 Numbers API([링크](https://rapidapi.com/divad12/api/numbers-1))를,
문제의 영->한 번역을 위해 Google Translate API([링크](https://rapidapi.com/undergroundapi-undergroundapi-default/api/google-translate113/))를 사용합니다.<br>
각각의 키를 `api_key.xml.example`의 `YOUR_API_KEY`에 채워 넣은 뒤 `api_key.xml`로 파일명을 변경해 `app/src/main/res/values/` 경로 아래에 넣어주세요.<br>
주의: Numbers API는 완전 무료이지만, Google Translate API는 **1,000건/월 까지만 무료**입니다 *(23/12/09 기준)*

## 라이선스
개별 파일에 다른 라이선스가 지정된 경우를 제외하고, 이 프로그램은 MIT License에 따라 배포합니다.