import cv2
from ultralytics import YOLO
import pandas as pd

save_dir = './pose_img/person/'
keypoint_path = f'{save_dir}keypoints.csv'

# YOLOv8 포즈 추정 모델 불러오기
model = YOLO("yolov8n-pose.pt")

# 카메라 또는 비디오 캡처 객체 생성 (0은 기본 웹캠)
cap = cv2.VideoCapture(0)

# 비디오의 총 프레임 수와 초당 프레임 수 가져오기
frames = cap.get(cv2.CAP_PROP_FRAME_COUNT)
fps = cap.get(cv2.CAP_PROP_FPS)

# 비디오의 총 시간 계산 (초 단위)
seconds = round(frames / fps)

# 처리할 프레임 수 설정 (필요에 따라 변경 가능)
frame_total = 500  
i = 0
a = 0

# 모든 데이터를 저장할 리스트
all_data = []

# 비디오가 열려 있는 동안 프레임 처리
while (cap.isOpened()):
  
    # 처리할 특정 시간(밀리초 단위)에 해당하는 프레임으로 이동
    cap.set(cv2.CAP_PROP_POS_MSEC, (i * ((seconds / frame_total) * 1000)))
    
    # 현재 프레임 읽기
    flag, frame = cap.read()

    # 더 이상 프레임이 없으면 종료
    if flag == False:
        break

    # 600번째 프레임 이상이면 종료
    if i >= 600:
        break
  
    # YOLOv8 모델로 프레임에서 객체 감지 수행
    results = model(frame, verbose=False)

    # 감지된 결과에 대해 반복 처리
    for r in results:
        bound_box = r.boxes.xyxy  # 프레임에서 경계 상자 좌표 가져오기
        conf = r.boxes.conf.tolist()  # 해당 객체가 사람일 확률 가져오기
        keypoints = r.keypoints.xyn.tolist()  # 프레임에서 감지된 각 사람의 키포인트 가져오기

        # 1개의 이미지에서 감지된 모든 사람 이미지를 저장하는 코드
        # 만약 1개의 이미지에 10명의 사람이 있다면, 10개의 사람 이미지를 저장

        for index, box in enumerate(bound_box):
            # 사람일 확률(conf)이 0.75 이상인 경우만 처리 (흐릿한 이미지 제외)
            if conf[index] > 0.75:
                x1, y1, x2, y2 = box.tolist()
                pict = frame[int(y1):int(y2), int(x1):int(x2)]
                output_path = f'{save_dir}person_{a}.jpg'

                # CSV 파일에 저장할 이미지 파일 이름
                data = {'image_name': f'person_{a}.jpg'}

                # 각 키포인트의 x와 y 좌표를 저장
                for j in range(len(keypoints[index])):
                    data[f'x{j}'] = keypoints[index][j][0]
                    data[f'y{j}'] = keypoints[index][j][1]

                # YOLO 모델이 감지한 사람의 키포인트를 나중에 기계 학습 모델에 학습시키기 위해 CSV 파일에 저장
                all_data.append(data)
                cv2.imwrite(output_path, pict)
                a += 1

    # 프레임 카운터 증가
    i += 1

# 처리한 총 프레임 수와 저장한 총 이미지 수 출력
print(i - 1, a - 1)
cap.release()
cv2.destroyAllWindows()

# 모든 데이터를 DataFrame으로 변환
df = pd.DataFrame(all_data)

# DataFrame을 CSV 파일로 저장
df.to_csv(keypoint_path, index=False)
