import { useState, createContext} from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'
export const GlobalContext = createContext();

function App() {
  const [count, setCount] = useState(0)

  const Props = {

  }

  const getAllItem = async () => {
    let allItem = await fetch("/todo")
    allItem = await allItem.json()
    console.log(allItem)
  }

  return (
    <>
      <button onClick={()=>{getAllItem()}}>get</button>
      <div className="card">
        <button onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </button>
        <p>
          Edit <code>src/App.jsx</code> and save to test HMR
        </p>
      </div>
      <p className="read-the-docs">
        Click on the Vite and React logos to learn more
      </p>
    </>
  )
}

export default App
