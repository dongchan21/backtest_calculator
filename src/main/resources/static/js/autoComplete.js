// static/js/autoComplete.js

/**
 * 인풋 요소에 oninput 이벤트를 연결해두면,
 * 사용자가 입력할 때마다 이 함수가 호출된다.
 */
async function onTickerInput(inputElem, listElemId) {
    const query = inputElem.value.trim();
    // listElemId: 자동완성 리스트를 표시할 div 혹은 ul의 id

    const listContainer = document.getElementById(listElemId);
    if (!listContainer) return;

    // 입력이 없으면 목록 숨기기
    if (!query) {
        listContainer.innerHTML = '';
        listContainer.style.display = 'none';
        return;
    }

    try {
        // 백엔드 검색 API 호출
        const response = await fetch(`/api/tickers?query=${query}`);
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await response.json();
        // data: Array of {symbol: "...", fullName: "..."}

        if (data && data.length > 0) {
            listContainer.innerHTML = '';
            data.forEach(item => {
                const div = document.createElement('div');
                div.className = 'autocomplete-item';
                div.textContent = item.fullName;

                // 항목을 클릭하면 input에 symbol을 세팅
                div.addEventListener('click', () => {
                    inputElem.value = item.symbol;
                    listContainer.innerHTML = '';
                    listContainer.style.display = 'none';
                });
                listContainer.appendChild(div);
            });
            listContainer.style.display = 'block';
        } else {
            listContainer.innerHTML = '';
            listContainer.style.display = 'none';
        }
    } catch (err) {
        console.error('Error searching tickers:', err);
    }
}
