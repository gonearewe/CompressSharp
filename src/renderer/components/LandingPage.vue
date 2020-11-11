<template>
  <div>
    <el-steps :active="1" simple>
      <el-step title="步骤 1"></el-step>
      <el-step title="步骤 2"></el-step>
      <el-step title="步骤 3"></el-step>
      <el-step title="步骤 3"></el-step>
      <el-step title="步骤 3"></el-step>
      <el-step title="步骤 3"></el-step>
      <el-step title="步骤 3"></el-step>
      <el-step title="步骤 3"></el-step>
      <el-step title="步骤 3"></el-step>
    </el-steps>
    <el-table
      ref="multipleTable"
      :data="fileList"
      tooltip-effect="dark"
      style="width: 100%"
      @row-dblclick="handleEnterPath"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55"> </el-table-column>
      <el-table-column label="name" width="120"></el-table-column>
      <el-table-column prop="size" label="姓名" width="120"> </el-table-column>
      <el-table-column prop="type" label="地址"></el-table-column>
      <el-table-column prop="last modified time" label="地址" show-overflow-tooltip></el-table-column>
      <el-table-column prop="CRC32" label="地址" show-overflow-tooltip></el-table-column>
    </el-table>
    <textarea v-model="res">
    </textarea>
    <el-button @click="handleClick">
    </el-button>
  </div>
</template>

<script>
const sevenZip = require('es-7z')

sevenZip.delete7z('E:\\新建文件夹.zip', 'E:\\新建文件夹.zip\\wget-log').progress(function (files) {
  console.log(files)
})

export default {
  data () {
    return {
      fileList: [
      ],
      multipleSelection: [],
      res: null
    }
  },

  methods: {
    toggleSelection (rows) {
      if (rows) {
        rows.forEach((row) => {
          this.$refs.multipleTable.toggleRowSelection(row)
        })
      } else {
        this.$refs.multipleTable.clearSelection()
      }
    },
    handleSelectionChange (val) {
      this.multipleSelection = val
    },
    handleEnterPath (row, column, event) {}
    // handleClick () {
    //   sevenZip.list('C:\\Users\\John Mactavish\\Desktop\\tmp\\新建文件夹.7z').on(
    //     'data', function (data) {
    //       this.res = data
    //     }
    //   )
    // }
  }
}
</script>
