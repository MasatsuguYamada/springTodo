import './App.css'
import {useEffect,useState} from "react";
import axios from "axios";

interface TodoItem {
  PK: string;
  text: string;
}

function App() {
  const [allItems, setAllItems] = useState<TodoItem[]>([])
  const [text, setText] = useState("")
  const [pk, setPk] = useState("291817bb-9a2c-414a-bbbf-eb9f3bd5a1e0")
  const [isTextClick, setIsTextClick] = useState(false)
  const [isButtonClick, setIsButtonClick] = useState(false)

  useEffect(() => {
    axios.get("/todo")
      .then(response => {
        console.log(response.data)
        setAllItems(response.data)
      })
    console.log("useEffect")
  }, [isButtonClick]);

  const todoItemClick = (todoItem: TodoItem) => {
    setText(todoItem.text)
    setPk(todoItem.PK)
    console.log(todoItem)
    setIsTextClick(true)
  }

  return (
    <>
      <h1>Todo List</h1>
      {allItems.map((todoItem, index) => {
        return (
          <>
            <div key={index}>
              <div style={{display: 'inline', marginRight: '20px' }}
                   onClick={() => {
                     todoItemClick(todoItem)
                   }}>Item{index + 1} : {todoItem.text}
              </div>
              <div style={{display: 'inline'}}
                   onClick={() => {

                   }}>
              </div>
            </div>
          </>
        )
      })}
      <div>
        <input type="text" value={text} onChange={(e) => setText(e.target.value)}/>
        <div>
          {!isTextClick ?
            <>
              <button onClick={() => {
                axios.post(`/todo`, {text: text})
                  .then(() => {
                    axios.get(`/todo`)
                      .then(response => {
                        console.log(response.data)
                        setAllItems(response.data)
                      })
                  })
                setText("")
                setIsButtonClick(!isButtonClick)
              }}>追加
              </button>
            </> : <>
              <button onClick={() => {
                axios.delete(`/todo/${pk}`)
                  .then(()=>{
                    setIsTextClick(false)
                    setText("")
                    setIsButtonClick(!isButtonClick)
                  })
              }}>
                削除
              </button>

              <button onClick={() => {
                axios.put(`/todo/${pk}`, {text: text})
                  .then(() => {
                    setIsTextClick(false)
                    setText("")
                    setIsButtonClick(!isButtonClick)
                  })

              }}>
                更新
              </button>

              <button onClick={() => {
                setIsTextClick(false)
                setIsButtonClick(!isButtonClick)
                setText("")
              }}>
                戻る
              </button>
            </>
          }
        </div>


      </div>

    </>
  )
}

export default App
