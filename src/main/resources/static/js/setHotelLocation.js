function setHotelLocationBasedOnAddress(address) {

    address = address.trim().toLowerCase();  // 공백 제거 및 소문자 변환

    let location = '';

    // 서울 지역 인근 구별 그룹핑
    // 서울 지역 그룹핑
    if (address.includes('서울 강남') || address.includes('서울 역삼') || address.includes('서울 삼성')) {
        location = '강남/역삼/삼성';
    } else if (address.includes('서울 신사') || address.includes('서울 청담') || address.includes('서울 압구정')) {
        location = '신사/청담/압구정';
    } else if (address.includes('서울 서초') || address.includes('서울 교대') || address.includes('서울 사당')) {
        location = '서초/교대/사당';
    } else if (address.includes('서울 잠실') || address.includes('서울 송파') || address.includes('서울 강동')) {
        location = '잠실/송파/강동';
    } else if (address.includes('서울 마포구 동교동') || address.includes('서울 마포구 합정') || address.includes('서울 마포구 상암') || address.includes('서울 마포구')) {
        location = '홍대/합정/상암/마포';
    } else if (address.includes('서울 구로') || address.includes('서울 신도림') || address.includes('서울 금천')) {
        location = '구로/신도림/금천';
    } else if (address.includes('서울 김포공항') || address.includes('서울 강서') || address.includes('서울 양천')) {
        location = '김포공항/강서/양천';
    } else if (address.includes('서울 건대입구') || address.includes('서울 성수') || address.includes('서울 성동')) {
        location = '건대입구/성수/성동';
    } else if (address.includes('서울 종로') || address.includes('서울 광화문') || address.includes('서울 명동') || address.includes('서울 청계천')) {
        location = '종로/광화문/청계천/명동';
    } else if (address.includes('서울 동대문') || address.includes('서울 을지로') || address.includes('서울 중구')) {
        location = '동대문/을지로/중구';
    } else if (address.includes('서울 용산') || address.includes('서울 이태원') || address.includes('서울 여의도')) {
        location = '용산/이태원/여의도';
    } else if (address.includes('서울 노원') || address.includes('서울 중랑') || address.includes('서울 도봉')) {
        location = '노원/중랑/도봉';
    } else if (address.includes('서울 성북') || address.includes('서울 광진')) {
        location = '성북/광진';
    } else if (address.includes('서울 강북') || address.includes('서울 도봉구') || address.includes('서울 노원구')) {
        location = '강북구/도봉구/노원구';
    } else if (address.includes('서울 은평구') || address.includes('서울 서대문구') ) {
        location = '은평구/서대문구';
    } else if (address.includes('서울 양천구') || address.includes('서울 강서구') || address.includes('서울 구로구')) {
        location = '양천구/강서구/구로구';
    } else if (address.includes('서울 동작구') || address.includes('서울 관악구') || address.includes('서울 금천구')) {
        location = '동작구/관악구/금천구';
    }



    // 경기도 (도 이름 없이 도시별 처리)
    else if (address.includes('경기 수원')) {
        location = '경기도 수원시';
    } else if (address.includes('경기 용인')) {
        location = '경기도 용인시';
    } else if (address.includes('경기 성남')) {
        location = '경기도 성남시';
    } else if (address.includes('경기 고양')) {
        location = '경기도 고양시';
    } else if (address.includes('경기 화성')) {
        location = '경기도 화성시';
    } else if (address.includes('경기 부천')) {
        location = '경기도 부천시';
    } else if (address.includes('경기 광명') || address.includes('경기 시흥')) {
        location = '광명/시흥';
    } else if (address.includes('경기 안양') || address.includes('경기 군포') || address.includes('경기 의왕') || address.includes('경기 과천')) {
        location = '안양/군포/의왕/과천';
    } else if (address.includes('경기 안산') || address.includes('경기 오산')) {
        location = '안산/오산';
    } else if (address.includes('경기 평택') || address.includes('경기 이천') || address.includes('경기 여주') || address.includes('경기 안성')) {
        location = '평택/이천/여주/안성';
    } else if (address.includes('경기 의정부') || address.includes('경기 남양주') || address.includes('경기 구리')) {
        location = '의정부/남양주/구리';
    } else if (address.includes('경기 파주') || address.includes('경기 양주') || address.includes('경기 동두천')) {
        location = '파주/양주/동두천';
    } else if (address.includes('경기 가평') || address.includes('경기 양평') || address.includes('경기 여주')) {
        location = '가평/양평/여주';
    } else if (address.includes('경기 포천') || address.includes('경기 동두천') || address.includes('경기 연천')) {
        location = '포천/연천';
    } else if (address.includes('경기 하남') || address.includes('경기 광주')) {
        location = '하남/광주';
    }


    // 인천
    else if (address.includes('인천 송도') || address.includes('인천 연수') || address.includes('인천 남동')) {
        location = '송도/연수/남동';
    } else if (address.includes('인천 부평') || address.includes('인천 계양')) {
        location = '부평/계양';
    } else if (address.includes('인천 중구') || address.includes('인천 동구') || address.includes('인천 미추홀')) {
        location = '중구/동구/미추홀';
    } else if (address.includes('인천 서구') || address.includes('인천 강화') || address.includes('인천 옹진')) {
        location = '서구/강화/옹진';
    }

    // 부산
    else if (address.includes('부산 해운대') || address.includes('부산 수영') || address.includes('부산 남구')) {
        location = '해운대/수영/남구';
    } else if (address.includes('부산 중구') || address.includes('부산 남포') || address.includes('부산 영도')) {
        location = '중구/남포/영도';
    } else if (address.includes('부산 동래') || address.includes('부산 연제') || address.includes('부산 금정')) {
        location = '동래/연제/금정';
    } else if (address.includes('부산 사하') || address.includes('부산 강서') || address.includes('부산 사상')) {
        location = '사하/강서/사상';
    } else if (address.includes('부산 북구') || address.includes('부산 울주') || address.includes('부산 기장')) {
        location = '북구/울주/기장';
    }

    // 대구
    else if (address.includes('대구 중구') || address.includes('대구 동성로') || address.includes('대구 남구')) {
        location = '중구/동성로/남구';
    } else if (address.includes('대구 수성구') || address.includes('대구 달서구') || address.includes('대구 북구')) {
        location = '수성구/달서구/북구';
    } else if (address.includes('대구 서구') || address.includes('대구 동구') || address.includes('대구 달성군')) {
        location = '서구/동구/달성군';
    } else if (address.includes('대구 군위군') || address.includes('대구 고령군') || address.includes('대구 청도군') || address.includes('대구 칠곡군')) {
        location = '군위군/고령군/청도군/칠곡군';
    }

    // 광주
    else if (address.includes('광주 동구') || address.includes('광주 서구') || address.includes('광주 남구')) {
        location = '동구/서구/남구';
    } else if (address.includes('광주 북구') || address.includes('광주 광산구')) {
        location = '북구/광산구';
    }

    // 대전
    else if (address.includes('대전 유성구') || address.includes('대전 서구') || address.includes('대전 중구')) {
        location = '유성구/서구/중구';
    } else if (address.includes('대전 동구') || address.includes('대전 대덕구')) {
        location = '동구/대덕구';
    }

    // 울산
    else if (address.includes('울산 남구') || address.includes('울산 중구') || address.includes('울산 동구')) {
        location = '남구/중구/동구';
    } else if (address.includes('울산 북구') || address.includes('울산 울주군')) {
        location = '북구/울주군';
    }

    // 강원
    else if (address.includes('강원특별자치도 강릉') || address.includes('강원특별자치도 동해') || address.includes('강원 삼척')) {
        location = '강릉/동해/삼척';
    } else if (address.includes('강원특별자치도 속초') || address.includes('강원특별자치도 양양')) {
        location = '속초/양양';
    } else if (address.includes('강원특별자치도 고성') || address.includes('강원특별자치도 동해') || address.includes('강원특별자치도 강릉')) {
        location = '고성/동해/강릉';
    } else if (address.includes('강원특별자치도 춘천') || address.includes('강원특별자치도 홍천') || address.includes('강원 평창')) {
        location = '춘천/홍천/평창';
    } else if (address.includes('강원특별자치도 원주') || address.includes('강원특별자치도 횡성') || address.includes('강원 영월')) {
        location = '원주/횡성/영월';
    } else if (address.includes('강원특별자치도 정선') || address.includes('강원특별자치도 태백') || address.includes('강원특별자치도 사북')) {
        location = '정선/태백/사북';
    } else if (address.includes('강원특별자치도 철원') || address.includes('강원특별자치도 화천') || address.includes('강원특별자치도 양구') || address.includes('강원특별자치도 인제')) {
        location = '철원/화천/양구/인제';
    }


    // 충북
    else if (address.includes('충북 청주') || address.includes('충북 충주') || address.includes('충북 제천')) {
        location = '청주/충주/제천';
    } else if (address.includes('충북 보은') || address.includes('충북 옥천') || address.includes('충북 영동')) {
        location = '보은/옥천/영동';
    } else if (address.includes('충북 진천') || address.includes('충북 음성') || address.includes('충북 증평')) {
        location = '진천/음성/증평';
    } else if (address.includes('충북 괴산') || address.includes('충북 단양')) {
        location = '괴산/단양';

        // 충남
    } else if (address.includes('충남 천안') || address.includes('충남 아산') || address.includes('충남 당진')) {
        location = '천안/아산/당진';
    } else if (address.includes('충남 논산') || address.includes('충남 계룡') || address.includes('충남 금산')) {
        location = '논산/계룡/금산';
    } else if (address.includes('충남 서산') || address.includes('충남 홍성') || address.includes('충남 예산')) {
        location = '서산/홍성/예산';
    } else if (address.includes('충남 서천') || address.includes('충남 보령')) {
        location = '서천/보령';

        // 세종
    } else if (address.includes('세종특별자치시')) {
        location = '세종시';
    }

    // 전북
    else if (address.includes('전북특별자치도 전주') || address.includes('전북특별자치도 군산') || address.includes('전북특별자치도 익산')) {
        location = '전주/군산/익산';
    } else if (address.includes('전북특별자치도 정읍') || address.includes('전북특별자치도 남원') || address.includes('전북특별자치도 완주')) {
        location = '정읍/남원/완주';
    }

    // 전남
    else if (address.includes('전남 여수') || address.includes('전남 순천') || address.includes('전남 광양')) {
        location = '여수/순천/광양';
    } else if (address.includes('전남 목포') || address.includes('전남 나주') || address.includes('전남 무안')) {
        location = '목포/나주/무안';
    } else if (address.includes('전남 담양') || address.includes('전남 순천') || address.includes('전남 해남')) {
        location = '담양/순천/해남';
    }

    // 경북
    else if (address.includes('경북 포항') || address.includes('경북 경주') || address.includes('경북 영천')) {
        location = '포항/경주/영천';
    } else if (address.includes('경북 구미') || address.includes('경북 안동') || address.includes('경북 문경')) {
        location = '구미/안동/문경';
    } else if (address.includes('경북 김천') || address.includes('경북 상주') || address.includes('경북 칠곡')) {
        location = '김천/상주/칠곡';
    } else if (address.includes('경북 영주') || address.includes('경북 의성') || address.includes('경북 청송')) {
        location = '영주/의성/청송';
    } else if (address.includes('경북 울진') || address.includes('경북 영덕')) {
        location = '울진/영덕';
    }

    // 경남
    else if (address.includes('경남 창원') || address.includes('경남 김해') || address.includes('경남 양산')) {
        location = '창원/김해/양산';
    } else if (address.includes('경남 진주') || address.includes('경남 사천') || address.includes('경남 밀양')) {
        location = '진주/사천/밀양';
    } else if (address.includes('경남 통영') || address.includes('경남 거제') || address.includes('경남 고성')) {
        location = '통영/거제/고성';
    } else if (address.includes('경남 함안') || address.includes('경남 의령') || address.includes('경남 창녕')) {
        location = '함안/의령/창녕';
    } else if (address.includes('경남 하동') || address.includes('경남 남해')) {
        location = '하동/남해';
    }



    // 제주도
    else if (address.includes('제주특별자치도 서귀포시 대림') ||address.includes('제주특별자치도 서귀포시 성산읍') || address.includes('제주특별자치도 서귀포시 표선면') || address.includes('제주특별자치도 서귀포시 송악산')) {
        location = '성산/표선/송악산';
    } else if (address.includes('제주특별자치도 제주시 구좌읍') || address.includes('제주특별자치도 제주시 조천읍') || address.includes('제주특별자치도 제주시 용담동')) {
        location = '구좌/조천/용담동';
    } else if (address.includes('제주특별자치도 서귀포시 남원읍') || address.includes('제주특별자치도 서귀포시 한경면') || address.includes('제주특별자치도 서귀포시 성산읍')) {
        location = '남원/한경/성산';
    } else if (address.includes('제주특별자지토 제주시 공항') || address.includes('제주특별자치도 제주시 서광로') || address.includes('제주특별자치도 제주시 동문시장')) {
        location = '제주공항/서광로/동문시장';
    } else if (address.includes('제주특별자치도 서귀포시 안덕면') || address.includes('제주특별자치도 서귀포시 호근동') || address.includes('제주특별자치도 서귀포시 대포동')) {
        location = '안덕면/호근동/대포';
    } else if (address.includes('제주특별자치도 제주시 이호동') || address.includes('제주특별자치도 제주시 용수리') || address.includes('제주특별자치도 제주시 제주대학교')) {
        location = '이호동/용수리/제주대학교';
    } else if (address.includes('제주특별자치도 제주시 중산간도로') || address.includes('제주특별자치도 제주시 천지연폭포') || address.includes('제주특별자치도 제주시 미로공원')) {
        location = '중산간도로/천지연폭포/미로공원';
    }


    // 모든 조건들이 맞지 않았을 때 or 해당 조건이 맞을 때만 적용해야함
    else if (address.includes('제주특별자치도 제주시 한림읍') || address.includes('제주특별자치도 제주시 애월읍') || address.includes('제주특별자치도 제주시')) {
        location = '제주/한림/애월';
    } else if (address.includes('제주특별자치도 서귀포시 중문') || address.includes('제주특별자치도 서귀포시 대정읍') || address.includes('제주특별자 서귀포시')) {
        location = '서귀포/중문/대정';
    }



    document.getElementById('hotelLocation').value = location;
}
